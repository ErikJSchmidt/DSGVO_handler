# SVM GDPR Classifier Training Set
### Goal
Train a Support Vector Machine (SVM) to tell apart a GDPR text from a text about something else.

### Training set
**1_create_svm_training_set_from_dev_dataset.ipynb**  
Given the websites with German text in the train_dataset construct a training set with all
the manually extracted GDPR texts as positive samples. From the websites that do not contain any 
GDPR content take texts of similar lengths as negative samples.

### Config
Path to the train dataset and location of the created SVM training set are specified in the 
**create_svm_training_set_from_dev_dataset_config.json**

### Testing the SVM
**2_test_sev.ipynb**
Use the before created training set at _data/svm_training_sets/svm_training_set_v1.json_
to train a SVM and check, that it learns. Without normalizing the TF-IDF vectors via RobustScaler
the SVM fails to improve during training in many runs.