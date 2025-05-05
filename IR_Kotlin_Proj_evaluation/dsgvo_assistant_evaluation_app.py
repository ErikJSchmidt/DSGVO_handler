import pandas as pd
import streamlit as st




df = pd.read_json(
    "/home/erik/Desktop/WiSe2022_23/IR-Project/dsgvo_handler/IR_Kotlin_Proj_evaluation/evaluated_test_set.json"
)


sample_nr = st.number_input("Select sample to view", 0, len(df) -1 )

sample = df.iloc[sample_nr]

url_col, label_col = st.columns(2)

with url_col:
    st.markdown(
        f"""
        **URL:**  
        [{sample["url"]}]({sample["url"]})
        """)

with label_col:
    st.markdown(
        f"""
        **Contains Dsgvo:**  
        {sample["contains_GDPR"]}
        """)

manual_count_col, assistant_count_col, error_col, error_rel_col = st.columns(4)

with manual_count_col:
    st.markdown(
        f"""
        **Manually removed tokens:**  
        {sample["manual_removed_count"]}
        """)

with assistant_count_col:
    st.markdown(
        f"""
        **Assistant removed tokens:**  
        {sample["assistant_removed_count"]}
        """)

with error_col:
    st.markdown(
        f"""
        **Removed tokens dif:**  
        {sample["content_removed_error"]}
        """)

with error_rel_col:
    st.markdown(
        f"""
        **Removed tokens dif/manual removed tokens:**  
        {sample["content_removed_error_rel"]}
        """)

st.markdown("### Content removed by assistant")
st.write(
    sample["content_removed_assistant"]
)

st.markdown("### Content removed manually (groundtruth)")
st.write(
    sample["content_removed"]
)