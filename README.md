# Reasoning_Agents_2020
Project repository for the course of Reasoning Agents 2020, Sapienza University of Rome.

https://www.canva.com/design/DAD8-U_KgHY/8uHCv_Ko46ZrQyIf6mbM8w/view?website#2

## NL2LTLf translation for restraining bolts application in BabyAI environment

This repository contains the tools and APIs used to perform natural language translation into Linear Temporal Logic over finite traces (LTLf) and the implementation of Restraining Bolts within the BabyAI platform.


The project was presented on May the 26th 2020.

Contents:

- [Structure](#structure)
-	[NL2LTLf](#NL2LTLf)
- [LTLf2DFA](#LTLf2DFA)
- [BabyAI and Restraining Bolts](#BabyAI-and-Restraining-Bolts)
- [References](#references)
- [Team Members](#team-members)

## Structure
- **NL2LTLf:** Group the three approaches for NL2LTLf translation
  - **Cfg** folder contains the implementation of the approach based on Context-Free Grammars as well as an example of the required grammars.
  - **LambdaCalculus** folder contains the implementation of the approach based on lambda-calculus as well as examples of mapping files.
  - **NLPPipeline/src** folder contains the implementation of the approach based on the NLP pipeline.
- **Video:** Contains the videos of the experiments performed in this work +Still to be updated!+
  - **Experiment1** folder contains all the video regarding the first experiment.
  - **Experiment2** folder contains all the video regarding the second experiment.
  - **Experiment3** folder contains all the video regarding the third experiment.
- **babyai_rb:** Contains babyai environment and the restraining bolt implementation +Still to be updated!+
- **[RA] Project Presentation.pdf** is the PDF presentation of this work.


## NL2LTLf
We proposed three different approaches for natural language translation into LTLf formulae.

### CFG-based
#### Installation
Requirements:
- Python 3.5+
- NLTK 3.0+
#### Usage
To run the translator, in the `Cfg` folder enter:

```
python ./CFG2LTLf.py --pathNL './cfg_nl.txt' --pathLTLf './cfg.ltlf' --sentence 'Go to a red ball'
```
use `--sentence` to input the sentence to translate, `--pathNL` to specify the path to the NL CFG and `--pathLTLf` to specify the path to the LTLf CFG.

### λCalculus-based
#### Installation
Requirements:
- Python 3.5+
- NLTK 3.0+
#### Usage
To run the translator, in the `LambdaCalculus` folder enter:

```
python ./NL2LTLf.py --path './mappings.csv' --sentence 'Go to a red ball' --set_pronouns 'True'
```
use `--sentence`  to input the sentence to translate, `--path` to specify the path to the mapping `.csv` file and `--set_pronouns` to enable/disable pronoun handling.

### NLP Based
#### Installation
Requirement **Java 1.8+**
The repository already contains **WordNet 2.1**, **VerbNet 3.0** and **StanfordCoreNLP API 4.0**. Language model must be downloaded.
* Copy `NLPPipeline/src/` on your project
* Go to `src/lib/` and follow `"IMPORTANT -model download.txt"` to download the language model
#### Run
Javadocs for all auxiliary classes are written. 
To translate a sentence use `NL2LTLTranslator.translate(sentence)`.
The class `NL2LTLTranslator` contains a main method with some examples.

## LTLf2DFA
To generate the Deterministic Finite State Automata (DFAs) use the FFloat tool, avaiable here:
`https://flloat.herokuapp.com/`

## BabyAI and Restraining Bolts
// ToDo

## Presentation

The source of the slides can be found at: `https://www.canva.com/design/DAD8-U_KgHY/8uHCv_Ko46ZrQyIf6mbM8w/view?utm_content=DAD8-U_KgHY&utm_campaign=designshare&utm_medium=link&utm_source=sharebutton`

## References

The papers used in this work include, but are not limited to:
- Chevalier-Boisvert, M., Bahdanau, D., Lahlou, S., Willems, L., Saharia, C., Nguyen, T. H., & Bengio, Y. (2018, September). BabyAI: A Platform to Study the Sample Efficiency of Grounded Language Learning. In International Conference on Learning Representations.
- Brunello, A., Montanari, A., & Reynolds, M. (2019). Synthesis of LTL Formulas from Natural Language Texts: State of the Art and Research Directions. In 26th International Symposium on Temporal Representation and Reasoning (TIME 2019). Schloss Dagstuhl-Leibniz-Zentrum fuer Informatik.
- De Giacomo, G., Iocchi, L., Favorito, M., & Patrizi, F. (2019, July). Foundations for restraining bolts: Reinforcement learning with LTLf/LDLf restraining specifications. In Proceedings of the International Conference on Automated Planning and Scheduling (Vol. 29, No. 1, pp. 128-136).
- J. Dzifcak, M. Scheutz, C. Baral and P. Schermerhorn, What to do and how to do it: Translating natural language directives into temporal and dynamic logic representation for goal management and action execution. 2009 IEEE International Conference on Robotics and Automation, Kobe, 2009, pp. 4163-4168, doi: 10.1109/ROBOT.2009.5152776.
- Lignos, C., Raman, V., Finucane, C. et al. Provably correct reactive control from natural language. Auton Robot 38, pp. 89–105 (2015). https://doi.org/10.1007/s10514-014-9418-8.
- G. Sturla, 2017 (May, 26 ). A Two-Phased Approach for Natural Language Parsing into Formal Logic (Master’s thesis, Massachusetts Institute of Technology, Cambridge, Massachusetts). pp. 18-44. Retrieved from https://dspace.mit.edu/bitstream/handle/1721.1/113294/1016164771-MIT.pdf?sequence=1.
- M. Chen, (2018). Translating Natural Language into Linear Temporal Logic (RUCS publication, University of Toronto, Toronto, Ontario). Retrieved from https://rucs.ca/assets/2018/submissions/chen.pdf.
- C. Lu, R. Krishna, M. Bernstein and L. Fei-Fei, 2016. Visual Relationship Detection with Language Priors. European Conference on Computer Vision. Retrieved from https://cs.stanford.edu/people/ranjaykrishna/vrd/., Montanari, A., & Reynolds, M. (2019). Synthesis of LTL Formulas from Natural Language Texts: State of the Art and Research Directions. In 26th International Symposium on Temporal Representation and Reasoning (TIME 2019). Schloss Dagstuhl-Leibniz-Zentrum fuer Informatik.
- J. Schulman, F. Wolski, P. Dhariwal, A. Radford, O. Klimov (2017). Proximal Policy Optimization Algorithms. OpenAI. Retrieved from https://arxiv.org/abs/1707.06347.

## Team members

- Kaszuba Sara, 1695639.
- Postolache Emilian, 1649271.
- Ratini Riccardo, 1656801.
- Simionato Giada, 1822614.
