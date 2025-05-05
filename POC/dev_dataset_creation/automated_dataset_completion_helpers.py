from bs4 import BeautifulSoup
from bs4.element import Comment
import os


def tag_visible(element):
    if element.parent.name in ['style', 'script', 'head', 'title', 'meta', '[document]']:
        return False
    if isinstance(element, Comment):
        return False
    return True

def text_from_html(body):
    soup = BeautifulSoup(body, 'html.parser')
    texts = soup.findAll(text=True)
    visible_texts = filter(tag_visible, texts)
    return u" ".join(t.strip() for t in visible_texts)

def extract_html_plain_text(sample_subfolder_path):
    sample_original_html_path = f"{sample_subfolder_path}/page_source.html"

    with open(sample_original_html_path, 'r') as f:
        contents = f.read()

        text = text_from_html(contents)

        content_removed_path = f"{sample_subfolder_path}/content.txt"
        with open(content_removed_path, 'w+') as wf:
            wf.write(text)

def extract_gdpr_plain_text_if_gdpr_website(sample_subfolder_path):
    sample_removed_html_path = f"{sample_subfolder_path}/page_source_removed.html"
    if os.path.exists(sample_removed_html_path):
        with open(sample_removed_html_path, 'r') as f:
            contents = f.read()

            text = text_from_html(contents)
            content_removed_path = f"{sample_subfolder_path}/content_removed.txt"
            with open(content_removed_path, 'w+') as wf:
                wf.write(text)

def fasttext_language_predict(text, model):

    text = text.replace('\n', " ")
    prediction = model.predict([text])

    return prediction