# Run this script as web app via terminal:
#       streamlit run dev_dataset_browser_application.py --server.fileWatcherType none

import streamlit as st
from my_utils import GDPRDataset
from pathlib import Path
import json

import streamlit.components.v1 as components

st.set_page_config(layout="wide")

st.markdown("""
# GDPR Data Browser
Below you can inspect single samples/websites from the development dataset.
""")

# Load config for this application
config_file = open(f"{str(Path.cwd())}/dev_dataset_browser_application_config.json")
config = json.load(config_file)
config_file.close()

dataset_location = st.selectbox("Select which dataset you want to browse",
                                ["dev_dataset_path",
                                 "dev_dataset_refined_path",
                                 "dev_dataset_refined_intermediate_path"])


df = GDPRDataset.GDPRDataset(config[dataset_location]).dataframe
df['original_index'] = list(range(len(df)))

st.write(df.head(3))
st.write("Number of samples in dataset", len(df))

st.markdown("""
## Filter data
""")


german_only = st.checkbox('Only german websites')
if german_only:
    df = df[df['language'] == "__label__de"]

gdpr_only = st.checkbox('Only websites with GDPR content')
if gdpr_only:
    df = df[df['contains_GDPR'] == True]

df = df.sort_index()

st.markdown("""
## Select single samples
""")

filtered_row_position = st.number_input(
    f'Select sample between 0 and {len(df) -1}',
    min_value=0,
    max_value=len(df) - 1,
    step=1)


st.markdown(f"""
Sample at position **{filtered_row_position}** in filtered list and index **{df.index[filtered_row_position]}** in dataset corresponds to website [{df.iloc[filtered_row_position]['url']}]({df.iloc[filtered_row_position]['url']})
""")

col1, col2 = st.columns(2)

with col1:
    st.header("Crawled HTML")
    try:
        components.html(
            df.iloc[filtered_row_position]['page_source_html'],
            width=800,
            height=1200
        )
    except Exception as error:
        st.write(str(error))
with col2:
    st.header("Removed GDPR text")
    st.write(df.iloc[filtered_row_position]['content_removed'])





