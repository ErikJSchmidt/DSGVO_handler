# Dev Dataset Refinement
For the goal of this project to remove German GDPR content from German websites not all 
samples in the dev_dataset are relevant. Furthermore the dev_dataset contains some 
inconsistencies:
1. For some websites Selenium is not able to crawl the HTML, however extracting the GDPR
    HTML with Chrome dev tools still works. This results in samples with empty page source HTML
    where any classifier will not find any GDPR content. However the sample is labeled as containing
    GDPR, because I could extract GDPR elements manually.


## Cleaning of Dev Dataset
1. Only keep samples with German content
2. Remove samples with empty page source HTML  

The resulting dataframe is saved to dev_dataset_refined_intermediate. 
Based on this I review some websites manually again and clean of some that make problems.

## Manually Remove Websites that cause trouble

- 7
  https://www.bergwerk.ag/, GDPR not downloaded in html due to shadow root
- 60
  http://tx-group.de/, domain reseller, website mainly js-script
- 89
  https://www.computop.com, GDPR not downloaded in html due to shadow root
- 116
  https://www.desko.de/de/, GDPR not downloaded in html due to shadow root
- 134
  http://www.coprint.de/, GDPR not downloaded in html due to shadow root
- 139
  http://www.ebenefuenf.de/, GDPR not downloaded in html due to shadow root
- 141
  https://www.edcud.de/, GDPR not downloaded in html due to shadow root
- 196
  http://www.gmk.de, GDPR not downloaded in html due to shadow root
- 197
  http://www.datakleen.de/, site cannot be reached
- 229
  http://www.standleitungen.de/, GDPR is loaded via iframe and therefore not in downloaded html
  
The latest refinement is on the dataset that consists of 428 samples in dev_dataset.
It comes down to 299 samples in the refined dataset before manually looking into the
the websites where the removed content has words that don't appear in page source html.