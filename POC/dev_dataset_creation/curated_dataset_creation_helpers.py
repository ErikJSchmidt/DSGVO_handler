# Helper functions for notebook 1_curated_dataset_creation.ipynb
import os
from selenium.webdriver.support.ui import WebDriverWait
import time
from selenium.common.exceptions import TimeoutException
from selenium.common.exceptions import WebDriverException

def create_subfolder_for_sample(config, sample_index):
    # Create folder for that row
    try:
        os.makedirs(f"{config['dev_dataset_path']}/{str(sample_index)}")
    except Exception as error:
        print(error)

def open_sample_website_and_extract_html(config, seeds_df, web_driver, sample_index):
    row = seeds_df.loc[sample_index]
    extracted_html = None
    if row['approvedForCrawl']:
        try:
            web_driver.get(row['url'])
            timeout = 10
            WebDriverWait(web_driver, timeout)
            # sleep, because oftentimes GDPR popups are loaded within the first seconds
            time.sleep(5)
            extracted_html = web_driver.page_source
        except TimeoutException:
            extracted_html = "-1"
        except WebDriverException:
            extracted_html = "-1"
    # Store extracted html to sample subfoder
    html_file = open(f"{config['dev_dataset_path']}/{sample_index}/page_source.html", "w+")
    html_file.write(extracted_html)
    html_file.close()

def create_empty_GDPR_files(config, sample_index):
    # Create the other files if GDPR content is on the website
    cleaned_html_file = open(f"{config['dev_dataset_path']}/{sample_index}/page_source_cleaned.html", "w+")
    cleaned_html_file.close()
    removed_html_file = open(f"{config['dev_dataset_path']}/{sample_index}/page_source_removed.html", "w+")
    removed_html_file.close()