# -----------------------------------------------------------------------------------------------------------------------------------------------------------
# Script for Natural Language (NL) to finite Linear Temporal Logic (LTLf) formulas translation.
#
# @authors Kaszuba Sara (1695639), Postolache Emilian (1649271), Ratini Riccardo (1656801), Simionato Giada (1822614).
#
# CFG based approach.
# -----------------------------------------------------------------------------------------------------------------------------------------------------------

import nltk
from nltk.tree import ParentedTree
import sys
import re
import string
import argparse
import time

parser = argparse.ArgumentParser()
parser.add_argument('--pathNL', dest='pathNL', default='./cfg_nl.txt', type=str, help='Path to the NL CFG file')
parser.add_argument('--pathLTLf', dest='pathLTLf', default='./cfg_ltlf.txt', type=str, help='Path to the LTLf CFG file')
parser.add_argument('--sentence', dest='sentence', type=str, help='Sentence to translate')

args = parser.parse_args()


# --- Function that extracts the structure of the tree obtained with parsing. ---
# :param ptree: parentedTree obtained after parsing
# :param list_: empty list (recursive method)
# :return list_: ordered list of the production rules used in the parsing

def get_structure(ptree, list_):

    if type(ptree) == str:                  # leaf
        list_.append([ptree, None])
    else:
        c = [ptree.label()]
        for subtree in ptree:
            if type(subtree) == str:
                c.append(subtree)
            else:
                c.append(subtree.label())
        list_.append(c)
        for subtree in ptree:
            get_structure(subtree, list_)   # DFS visit
        

# --- Function that post processes the structure of the tree. ---
# :param list_: structure extracted from the tree
# :return list_: post processed version of the input

def post_processing(list_):
    
    upd_list = []
    for elem in list_:
        if len(elem)==1:            # if node with null production as child
            h = elem
            h.append('')
            upd_list.append(h)
        elif elem[1]==None:         # if leaf
            continue
        else:
            upd_list.append(elem)
    return upd_list


# --- Function that builds a dictionary from the production rules of a CFG. ---
# :param prod: string that encodes all the production rules
# :return dict_: dictionary whose keys are the heads of the production rules and as values the lists of the options

def ref_gram(prod):

    dict_ = {}
    str_prod = prod
    str_prod = str_prod.replace(' ', '')     # remove aestetical spaces
    str_prod = str_prod.replace('"', '')
    parts = str_prod.split('\n')             # split the production rules            
    parts = parts[1:-1]                      # strip the production set
    for elem in parts:
        head, opts = elem.split('->')
        opts = opts.split('|')
        dict_[head] = opts                   # add new element to dictionary
    return dict_


# --- Function that checks whether two grammars have the same structure. ---
# :param gr1: dictionary of the first grammar
# :param gr2: dictionary of the second grammar
# :return bool: whether they have the same structure

def check_consistency(gr1, gr2):

    if len(gr1)!=len(gr2):                      # check whether they have the same number of productions
        return False
    for i, elem in enumerate(gr1):
        if len(gr1[elem]) != len(gr2[elem]):    # check if all productions have the same number of values
            return False
    return True


# --- Function that concatenates all the elements of a list. ---
# :param list_: list to zip
# :return z: string that is the concatenation of all the elements of list_

def zip_opt(list_):
    z = ''
    for elem in list_:
        z += elem
    return z


# --- Function that builds the LTLf formula. ---
# :param structure: list of lists that are the structure of the tree
# :param nlD: dictionary of productions of the CFG grammar for natural language sentences
# :param ltlD: dictionary of productions of the CFG grammar for linear temporal logic sentences
# :param inS: initial symbol of the NL grammar
# :return formula: LTLf formula

def get_formula(structure, nlD, ltlD, inS):
    
    formula = inS                               # start with initial symbol
    for step in structure:
        head = step[0]
        opt = zip_opt(step[1:])
        opt_indx = nlD[head].index(opt)
        opt_ltl = ltlD[head][opt_indx]
        formula = formula.replace(head, opt_ltl, 1)
    return formula


# --- Function that preprocesses the initial sentence. ---
# :param sentence: sentence to translate
# :param combo: list of elements composed by more than one word
# :return sentence: sentence preprocessed

def preprocess(sentence, combo):

    sentence = sentence.lower()
    exclude = set(string.punctuation)
    sentence = ''.join(ch for ch in sentence if ch not in exclude)  # remove punctuation
    replacements = [x.replace('_', ' ') for x in combo]
    for i, elem in enumerate(combo):
        sentence = sentence.replace(replacements[i], elem)          # remove spaces in combo elements with _
    return sentence


# --- Function that retrieves all combos. ---
# :param nlP: string that encodes the NL CFG
# :return occ: list of elements composed by more than one word

def get_combo(nlP):

    reg = '[a-z]+_[a-z]+[_a-z]*'     # RE encoding of multiple words divided by _   
    occ = re.findall(reg, nlP)
    return occ


# --- Function that simplifies the LTLf formula. ---
# :param formula: formula to simplify
# :return formula: simplified formula

def remove_doubles(formula):

    formula = re.sub('[F]+', 'F', formula)      # simplify chains of eventually
    formula = re.sub('[G]+', 'G', formula)      # simplify chains of always
    formula = re.sub('[U]+', 'U', formula)      # simplify chains of until
    formula = re.sub('(!!)+', '', formula)      # simplify chains of not (if even number, then removed; if odd, then only one is kept)
    formula = formula.replace('N', 'X')         # convert 'next' into formalism required by FFLOAT tool
    return formula


# --- Function that perdforms the grounding of the formula. ---
# :param formula: final lambda expression
# :return formula: LTLf grounded formula

def ground(formula):
    
    formula = formula.replace(',', '_')
    reg = '[a-z0-9]+\([a-z_0-9]+\)'
    occ = re.findall(reg, formula)              # find occurrences of the RE reg in the formula
    for elem in occ:
        formula = formula.replace(elem, elem.replace('(', '_').replace(')', ''))
        formula = remove_doubles(formula)
    return formula



if __name__ == "__main__":

    i = time.time()
    sentence = args.sentence
    path_NL = args.pathNL
    path_LTLf = args.pathLTLf

    f = open(path_NL, "r")
    prod_nl = f.read()
    f.close()

    grammar = nltk.CFG.fromstring(prod_nl)       # build grammar with production of NL language

    f = open(path_LTLf, "r")
    prod_ltl = f.read()
    f.close()

    prod_nl_dict = ref_gram(prod_nl)                             # get dictionary of first grammar
    prod_ltl_dict = ref_gram(prod_ltl)                           # get dictionary of second grammar
    
    if not check_consistency(prod_nl_dict, prod_ltl_dict):       # check whether the two grammars have the same structure
        print('+++ Grammars not consistent!')
        sys.exit()
    
    init_sym = re.findall('[A-Za-z0-9]+', prod_nl)[0]           # get initial symbol of the grammar
    combos = get_combo(prod_nl)                                 # get composite words

    #sent = "go to the blue ball and pick up a box"
    #sent = "Pick up a, green ball and, put a box next to a Key then open the door!!"
    sent = preprocess(sentence, combos)
    sent = sent.split()
    rd_parser = nltk.RecursiveDescentParser(grammar)            # parse sentence according to NL CFG
    for p in rd_parser.parse(sent):
        tree=p
    try:
        ptree = ParentedTree.convert(tree)                      # convert tree into parentedTree
    except:
        print("+++ The sentence doesn't belong to the language of the grammar.")
        sys.exit()
    structure = []
    get_structure(ptree, structure)                             # retieve structure
    structure = post_processing(structure)                      # fix structure
    print('Translated: ', sentence)
    LTLf = get_formula(structure, prod_nl_dict, prod_ltl_dict, init_sym)
    print('Into (not grounded): ', LTLf)
    LTLf = ground(LTLf)                                         # ground LTLf formula
    print('Into:', LTLf)
    f = time.time()
    print('Time elapsed: ', f-i, 's')