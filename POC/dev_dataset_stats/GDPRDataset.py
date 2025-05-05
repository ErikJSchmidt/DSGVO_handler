
import pandas as pd
import os


class GDPRDataset():

    def __init__(self, dataset_dir):
        self.dataset_dir = dataset_dir
        df = pd.read_json(f"{self.dataset_dir}/dev_dataset_languages.json")
        df.rename(columns={0:'language'}, inplace=True)

        urls_df = pd.read_json(f"{self.dataset_dir}/dev_dataset_urls.json")
        urls_df.rename(columns={0:'url'}, inplace=True)
        df['url'] = list(urls_df['url'].values)

        # Add empty columns for html and cleaned text to dataframe
        contents = []
        page_sources = []
        contents_removed = []
        page_sources_cleaned = []
        page_sources_removed = []


        # Row by row read in the values from the files
        for i in range(len(df)):

            def try_open_and_read_in_and_append(path, list):
                try:
                    with open(path) as f:
                        list.append(f.read())
                except:
                    list.append(None)

            try_open_and_read_in_and_append(self.dataset_dir + f"/{i}/content.txt", contents)

            try_open_and_read_in_and_append(self.dataset_dir + f"/{i}/page_source.html", page_sources)

            try_open_and_read_in_and_append(self.dataset_dir + f"/{i}/content_removed.txt", contents_removed)

            try_open_and_read_in_and_append(self.dataset_dir + f"/{i}/page_source_cleaned.html", page_sources_cleaned)

            try_open_and_read_in_and_append(self.dataset_dir + f"/{i}/page_source_removed.html", page_sources_removed)

        df['content'] = contents
        df['page_source_html'] = page_sources
        df['content_removed'] = contents_removed
        df['page_source_cleaned_html'] = page_sources_cleaned
        df['page_source_removed_html'] = page_sources_removed

        # Add a column to directly see which row contains GDPR content. If all three GDPR dependent files do not exist,
        # then this row had no GDPR content identified in it.
        df['contains_GDPR'] = ~(df['content_removed'].isna() & df['page_source_cleaned_html'].isna() & df['page_source_removed_html'].isna())

        self.dataframe = df

    def __len__(self):
        return len(self.dataframe)

    def __getitem__(self, item):
        return self.dataframe.loc[item]

    def store(self, dataset_dir=None):
        # As default store
        if dataset_dir == None:
            dataset_dir = self.dataset_dir
        languages = []
        urls = []
        for sample_index in range(len(self.dataframe)):
            sample_subfolder_path = f"{dataset_dir}/{str(sample_index)}"
            # create sample sample_subfolder_path
            try:
                os.makedirs(sample_subfolder_path)
            except Exception as error:
                print(error)

            sample_row = self.dataframe.loc[sample_index]

            # store html
            html_file = open(f"{sample_subfolder_path}/page_source.html", "w+")
            html_file.write(sample_row['page_source_html'])
            html_file.close()
            # store content
            content_file = open(f"{sample_subfolder_path}/content.txt", "w+")
            content_file.write(sample_row['content'])
            content_file.close()
            if self.dataframe.loc[sample_index]['contains_GDPR']:
                # store removed html
                html_removed_file = open(f"{sample_subfolder_path}/page_source_removed.html", "w+")
                html_removed_file.write(sample_row['page_source_removed_html'])
                html_removed_file.close()
                # store removed content
                content_removed_file = open(f"{sample_subfolder_path}/content_removed.txt", "w+")
                content_removed_file.write(sample_row['content_removed'])
                content_removed_file.close()
                # store cleaned html
                html_cleaned_file = open(f"{sample_subfolder_path}/page_source_cleaned.html", "w+")
                html_cleaned_file.write(sample_row['page_source_cleaned_html'])
                html_cleaned_file.close()

            languages.append(sample_row['language'])
            urls.append(sample_row['url'])

        languages_df = pd.DataFrame(languages)
        languages_df.to_json(f"{dataset_dir}/dev_dataset_languages.json")

        urls_df = pd.DataFrame(urls)
        urls_df.to_json(f"{dataset_dir}/dev_dataset_urls.json")
#%%
