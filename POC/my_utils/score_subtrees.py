"""
This script contains functions that map a html to a list of nodes ("html", "body", "div") where each node gets a score
how that shows how likely this node is to be a GDPR element based on this nodes's subtree text.
"""
import pandas as pd
from sklearn.preprocessing import RobustScaler
from joblib import load

from .text_preprocessing import clean_german_text, clean_german_texts
import sklearn.svm as svm

from bs4 import BeautifulSoup
from nltk.corpus import stopwords
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

class BaseSubtreeScorer:

    def get_scored_html_nodes(self, html):
        pass

    def _get_relevant_html_nodes(self, html):

        tag_names = []
        tag_classes = []
        subtree_htmls = []
        subtree_texts = []
        subtree_text_lengths = []
        soup = BeautifulSoup(html, features="html.parser")
        for element in soup.find_all(name=None, recursive=True):
            if element.name in ["html", "body", "div"]:
                try:
                    tag_class = element['class']
                except:
                    tag_class = "none"
                tag_classes.append(tag_class)
                tag_names.append(element.name)
                subtree_htmls.append(str(element))
                # get visible text within this div
                subtree_text = element.text.strip("\n").replace("\n", " ")
                subtree_texts.append(subtree_text)
                subtree_text_lengths.append(len(subtree_text))

        node_df = pd.DataFrame(data={'tag_name': tag_names,
                                     'class': tag_classes,
                                     'subtree_html': subtree_htmls,
                                     'subtree_text': subtree_texts,
                                     'subtree_text_length': subtree_text_lengths})

        node_df = node_df[node_df['subtree_text_length'] > 0]
        node_df.reset_index(inplace=True, drop=True)

        return node_df


class TFIDF_ref_doc_sim_subtree_scorer(BaseSubtreeScorer):

    def __init__(self, documents, reference_document_components, do_text_preprocessing):
        self.do_text_preprocessing = do_text_preprocessing

        # build TF-IDF vectorizer
        german_stop_words = stopwords.words('german')
        self.tfidf_vectorizer = TfidfVectorizer(analyzer='word', stop_words=german_stop_words)

        if self.do_text_preprocessing:
            fit_tfidf_documents = clean_german_texts(documents)
        else:
            fit_tfidf_documents = documents

        self.tfidf_vectorizer.fit_transform(fit_tfidf_documents)

        # build ref doc
        if self.do_text_preprocessing:
            self.composed_reference_document = clean_german_text(" ".join(reference_document_components))
        else:
            self.composed_reference_document = " ".join(reference_document_components)
        self.composed_reference_document_vectorized = \
            self.tfidf_vectorizer.transform([self.composed_reference_document])[0]

    def get_scored_html_nodes(self, html):
        # to score the text of each DOM subtree at first get a list of all subtree texts
        relevant_nods_df = self._get_relevant_html_nodes(html)

        GDPR_scores = []
        for i in range(len(relevant_nods_df)):
            subtree_text = relevant_nods_df.iloc[i]['subtree_text']
            if self.do_text_preprocessing:
                subtree_text = clean_german_text(subtree_text)
            subtree_text_vectorized = self.tfidf_vectorizer.transform([subtree_text])[0]

            score = cosine_similarity(self.composed_reference_document_vectorized, subtree_text_vectorized)[0][0]
            GDPR_scores.append(score)

        relevant_nods_df['GDPR_score'] = GDPR_scores

        return relevant_nods_df


class TFIDF_SVM_subtree_scorer(BaseSubtreeScorer):

    def __init__(self, documents, train_dataset_df, do_text_preprocessing, pretrained_paths=None):
        self.do_text_preprocessing = do_text_preprocessing

        if pretrained_paths:
            print("Use pretrained vetorizer, sclaer and SVM")
            # load pretrained model
            self.tfidf_vectorizer = load(pretrained_paths['vectorizer_path'])
            self.rbX = load(pretrained_paths['robust_scaler_path'])
            self.SVM = load(pretrained_paths['svm_model_path'])
        else:
            print("Fit vectorizer, sclaer and SVM to data")
            # build TF-IDF vectorizer
            german_stop_words = stopwords.words('german')
            self.tfidf_vectorizer = TfidfVectorizer(analyzer='word', stop_words=german_stop_words)

            if self.do_text_preprocessing:
                fit_tfidf_documents = clean_german_texts(documents)
            else:
                fit_tfidf_documents = documents

            self.tfidf_vectorizer.fit_transform(fit_tfidf_documents)

            # train or load SVM
            train_texts = train_dataset_df['text'].values
            if self.do_text_preprocessing:
                train_texts = clean_german_texts(train_texts)
            train_text_vectors = self.tfidf_vectorizer.transform(train_texts)
            self.rbX = RobustScaler(with_centering=False)
            train_text_vectors_norm = self.rbX.fit_transform(train_text_vectors)
            train_labels = train_dataset_df['is_GDPR'].values
            # train SVM here
            self.SVM = svm.SVC(C=1.0, kernel='rbf', degree=3, gamma='auto', probability=True)
            self.SVM.fit(train_text_vectors_norm, train_labels)

    def get_scored_html_nodes(self, html):
        # to score the text of each DOM subtree at first get a list of all subtree texts
        relevant_nods_df = self._get_relevant_html_nodes(html)

        GDPR_scores = []
        for i in range(len(relevant_nods_df)):
            subtree_text = relevant_nods_df.iloc[i]['subtree_text']
            if self.do_text_preprocessing:
                subtree_text = clean_german_text(subtree_text)
            subtree_text_vectorized = self.tfidf_vectorizer.transform([subtree_text])[0]

            probs_np_array = self.SVM.predict_proba(self.rbX.transform(subtree_text_vectorized))
            # access the first( and only) row
            probs = probs_np_array[0]
            # the second value corresponds to probability of label 1
            GDPR_prob = probs[1]

            GDPR_scores.append(GDPR_prob)

        relevant_nods_df['GDPR_score'] = GDPR_scores

        return relevant_nods_df
