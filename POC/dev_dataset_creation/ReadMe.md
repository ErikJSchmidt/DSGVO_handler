# Dev Dataset Creation
Given a list of 750 seed websites of interest a dataset for GDPR/DSGVO detection is curated.  
For each website we store:
- The url
- The raw html extracted with a Selenium Driver
- The text that is readable on the website/visible to visitors of the website  
- The language of the readable text (English ro German)

If a manual inspection of the websites shows that there is some GDPR content present we also store:
- The html element that wraps the GDPR content, e.g. the outer div and all html inside
- The html of the website after removing the GDPR related html from it
- The readable text of the GDPR element

### Python Jupyter Notebooks
To do so the notebook **1_curated_dataset_creation.ipynb** is used to iterate through the given websites, inspect each
site individually and store the respective htmls based on human judgment.  
The data that needs no human judgment (extraction of readable text, language, url) is added by running the notebook
**2_automated_dataset_completion.ipynb**.

### Config File
Location of input and output data is specified in the **dev_dataset_creation_config.json**

### Python Helpers
To keep both notebooks short and readable some function and details are outsourced to **curated_dataset_creation_helpers.py** and **automated_dataset_completion_helpers.py** respectively.