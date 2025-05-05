# dsgvo_handler
This repository contains code for the _DsgvoAssistant_ Kotlin module. This assistant provides
functionalities for handling pop ups related to the German data privacy law DSGVO.
The assistant can detect if the HTML of a website contains a DSGVO pop up and, if so, can remove it 
from the HTML.
This project is part of my Master in Applied Computer Science at the Otto-Friedrich-University in Bamberg.
It is embedded in the project IT-Atlas-Oberfranken, a vertical search engine for computer science related job offers.

# Overview
This repository contains three main directories:
- **POC**: Here you find the proof of concept. It consists of x main steps:
  - The creation of a dataset with 428 manually labeled websites
  - The refinement of this dataset
  - Statistics and insights from that dataset
  - Training of a SVM for text classification based on the dataset
  - Utilizing the text classifier to handle HTML with DSGVO content
- **IR_Kotlin_Proj**: The Kotlin project of the _DsgvoAssistant_. A finetuned and extended implementation of the poc approach
- **IR_Kotlin_Proj_evaluation**: Jupyter Notebooks for evaluating the output of the _DsgvoAssistant_ on a test dataset

# Project Report
For a more detailed description of the project please refer to the [project report](DsgvoAssistantProjectReport.pdf).
The slides of the [mid-term presentation](DSGVO_handler.pdf) were used to present the POC phase of this project and can
help too.

# Copyright and Licensing

MIT License

Copyright (c) 2024 Erik Jonathan Schmidt

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.