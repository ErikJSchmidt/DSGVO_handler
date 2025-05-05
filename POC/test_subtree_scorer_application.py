# Run this script as web app via terminal:
#       streamlit run test_subtree_scorer_application.py
# Make sure you downloaded the NLTK corpora used for text preprocessing in your environment:
#       python -m nltk.downloader stopwords
#       python -m nltk.downloader punkt

import pandas as pd
import numpy as np
import streamlit as st
from my_utils import GDPRDataset, score_subtrees
from sklearn.metrics import accuracy_score, confusion_matrix
from pathlib import Path
import json

import streamlit.components.v1 as components

st.set_page_config(layout="wide")

st.markdown("""
# GDPR scorer demo
Below you can inspect single samples/websites from the test dataset.
Select one scoring model and a website to inspect how this model scores the text of the website's subtrees.
""")

# Load config for this application
config_file = open(f"{str(Path.cwd())}/test_subtree_scorer_application_config.json")
config = json.load(config_file)
config_file.close()

# Load the datasets


df_train = GDPRDataset.GDPRDataset(dataset_dir=config['train_dataset_path']).dataframe
df_test = GDPRDataset.GDPRDataset(dataset_dir=config['test_dataset_path']).dataframe

selected_model = st.selectbox("Model", ["TF-IDF+cosine", "TF-IDF+SVM"])

if selected_model == "TF-IDF+cosine":

    st.markdown("""### TF-IDF+cosine  
    The TF-IDF Vectorizer is fit on all documents in GDPRDataset.  
    The GDPR reference document is constructed from all 'removed_content' texts in the GDPRDataset.
    """)

    preprocessing_mode = st.selectbox("Text preprocessing. Do clean up of text before vectorization",
                                      ["do_preprocessing", "no_preprocessing"])

    scoring_model = score_subtrees.TFIDF_ref_doc_sim_subtree_scorer(documents=list(df_train['content'].values),
        reference_document_components=list(df_train[df_train['contains_GDPR']]['content_removed'].values),
        do_text_preprocessing=preprocessing_mode == "do_preprocessing")



elif selected_model == "TF-IDF+SVM":
    st.markdown("""### TF-IDF+SVM 
    The TF-IDF Vectorizer is fit on all documents in GDPRDataset.  
    The SVM takes a training set created from the GDPRDataset.
    - It contains the 'removed_content' texts as positive samples
    - The negative samples need to be curated with care.
    """)

    tfidf_svm_train = pd.read_json(config['svm_training_set_v1_path'])

    preprocessing_mode = st.selectbox("Text preprocessing. Do clean up of text before vectorization",
                                      ["do_preprocessing", "no_preprocessing"])

    scoring_model = score_subtrees.TFIDF_SVM_subtree_scorer(
        documents=None,
        train_dataset_df=None,
        do_text_preprocessing=preprocessing_mode == "do_preprocessing",
        pretrained_paths={
            "svm_model_path": config['best_svm_model_path'],
            "vectorizer_path": config['vectorizer_path'],
            "robust_scaler_path": config['robust_scaler_path']
        }
    )

st.write(
    f" - Train set contains {len(df_train)} websites of which {len(df_train[df_train['contains_GDPR']])} contain GDPR elements.")

st.write(
    f" - Test set contains {len(df_test)} websites of which {len(df_test[df_test['contains_GDPR']])} contain GDPR elements.")

st.markdown(""" ## Do select single sample
    Select a sample from the test set and calculate GDPR probability scores for the texts of this website's DOM nodes.
    Review the row of the test sample in the GDPRDataset in section 'Sample row' and the highest socring DOM node below.
    
""")

sample_test_set_index = st.number_input(f'Select sample between 0 and {len(df_test) - 1}', min_value=0,
    max_value=len(df_test) - 1, step=1)

col1, col2 = st.columns(2)

with col1:
    st.markdown(f"""
    #### Sample row/Original website
    Corresponds to website [{df_test.iloc[sample_test_set_index]['url']}]({df_test.iloc[sample_test_set_index]['url']}):
    """)

    st.write(df_test.iloc[sample_test_set_index])

    selected_sample_html = df_test.iloc[sample_test_set_index]['page_source_html']
    scored_nodes_df = scoring_model.get_scored_html_nodes(selected_sample_html)

    try:
        components.html(df_test.iloc[sample_test_set_index]['page_source_html'], width=800, height=1200)
    except Exception as error:
        st.write(str(error))

    st.write(
        f"Inspected {len(scored_nodes_df)} nodes of the html." + f"Highest score is **{max(list(scored_nodes_df['GDPR_score'].values))}** at {np.argmax(list(scored_nodes_df['GDPR_score'].values))}")

with col2:
    st.markdown("""
    ### The highest scoring html node/subtree:
    """)

    st.write(scored_nodes_df.iloc[np.argmax(list(scored_nodes_df['GDPR_score'].values))])

    try:
        components.html(scored_nodes_df.iloc[np.argmax(list(scored_nodes_df['GDPR_score'].values))]['subtree_html'],
            width=800, height=1200)
    except Exception as error:
        st.write(str(error))

st.markdown("""
#### Classifier accuracy  
For each website take the highest score the model assigned to a subtree and classify the website based on this score 
against the chosen threshold.
""")

threshold = st.number_input(f'Set threshold between 0 adn 1', min_value=0.0, max_value=1.0, step=0.01)


def get_mertics_on_test_set():
    # calculate max subtree score
    test_set_htmls_df = pd.DataFrame(data={'test_set_position': list(range(len(df_test))),
        'page_source_html': list(df_test['page_source_html'].values), 'url': list(df_test['url'].values),
        'y': list(df_test['contains_GDPR'].values)})
    test_set_htmls_df['max_subtree_score'] = test_set_htmls_df['page_source_html'].apply(
        lambda html: max(list(scoring_model.get_scored_html_nodes(html)['GDPR_score'].values)))

    # parse scores + threshold to prediction
    test_set_htmls_df['prediction'] = test_set_htmls_df['max_subtree_score'].apply(lambda score: score >= threshold)

    st.dataframe(test_set_htmls_df)

    # get metrics
    Y = list(test_set_htmls_df['y'].values)
    Y = list(map(int, Y))

    Y_pred = list(test_set_htmls_df['prediction'].values)
    Y_pred = list(map(int, Y_pred))

    accuracy = accuracy_score(Y, Y_pred)

    tn, fp, fn, tp = confusion_matrix(Y, Y_pred).ravel()

    return {'acc': accuracy, 'fp': fp, 'fn': fn}


metrics = get_mertics_on_test_set()
st.write(metrics)
