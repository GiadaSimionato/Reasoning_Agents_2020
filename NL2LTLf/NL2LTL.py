# -----------------------------------------------------------------------------------------------------------------------------------------------------------
# Script for Natural Language (NL) to finite Linear Temporal Logic (LTLf) formulas translation.
#
# @author Giada Simionato <simionato.1822614@studenti.uniroma1.it>
#
# To deal with semantic ambiguities please modify .csv file.
# -----------------------------------------------------------------------------------------------------------------------------------------------------------

import nltk
import csv
import sys
import re
import argparse
import time

parser = argparse.ArgumentParser()
parser.add_argument('--path', dest='path', default='./mappings.csv', type=str, help='Path to the mapping file')
parser.add_argument('--sentence', dest='sentence', type=str, help='Sentence to translate')

args = parser.parse_args()


# --- Function that creates the dictionary from mapping table. ---
# :param path: path to the file .csv containing the mapping
# :return dict_: dictionary whose keys are the words and the values the lambda expressions

def load_mapping(path):

    dict_= {}
    line_count = 0
    print('Accessing the file...')
    with open(path) as csvfile:
        print('Loading data...')
        readCSV = csv.reader(csvfile, delimiter=';')
        for row in readCSV:
            line_count += 1
            if line_count-1==0:
                continue                        # ignore first line
            key = row[0]                        # word
            arg = row[1]                        # lambda-TL expression
            try:
                params, f = arg.split('.')
                params = params.split('$')[1:]  # list of params of expression (e.g. ['x','y'])
            except:                             # atomic proposition case (e.g. 'blue')
                params=[]
                f = arg
            value = [params, f]                 # list e.g. [['x', 'y'], 'G(x->y)']
            dict_[key] = value
    print('Done. Processed', line_count, 'lines.')
    return dict_


# --- Function that retrieves elements with more than one word. ---
# :param dict_: the mapping 
# :return list_: list of all the elements with more than one word

def get_composite(dict_):

    list_ = []
    for key in dict_:
        if ' ' in key:
            list_.append(key)
    return list_


# --- Function that retrieves the list of lambda expressions from tokenized sentence. ---
# :param sent: the sentence to translate
# :param dict_: the mapping
# :return list_: the list of the corresponding lambda expressions

def get_lambda_TL_list(sent, dict_):

    list_ = []
    for elem in sent:
        try:
            list_.append(dict_[elem.replace('_', ' ')])
        except:
            print(' +++ The word ', elem.replace('_', ' '), "doesn't have a mapping!")  # word not present in the mapping
            sys.exit()
    return list_


# --- Function that checks whether the elements of a list are all objects. ---
# :param exp: the list of lambda expressions to check
# :return bool: whether the element of the list are all objects

def areAllObjects(exp):

    for elem in exp:
        if elem[0]!=[]:
            return False
    return True


# --- Function that checks whether the elements of a list are all functions. ---
# :param exp: the list of lambda expressions to check
# :return bool: whether the element of the list are all functions

def areAllFunctions(exp):

    for elem in exp:
        if elem[0]==[]:
            return False
    return True


# --- Function that creates the list of the indices of the objects. ---
# :param exp: list of lambda expressions
# :return indeces: list of indices

def get_indexObj(exp):

    indices = []
    for i, elem in enumerate(exp):
        if elem[0]==[]:
            indices.append(i)
    return indices


# --- Function that applies the object to the function. ---
# :param obj: object
# :param func: lambda function
# :return list: a new expression derived from the application of the object to the function

def resolve(obj, func):

    value_obj = obj[1]
    value_func = func[1]
    param = func[0][0]
    value = value_func.replace(param, value_obj)
    return [func[0][1:], value]


# --- Function that does the iterative step of the recursive function. ---
# :param exp: list of lambda expressions
# :return exp: list of lambda expressions after the one step of recursion

def iter_step(exp):
   
    objs_indx = get_indexObj(exp)                   # list of indeces of objects
    while objs_indx != []:                          # until there are objects to be resolved
        i = objs_indx.pop(0)                        # remove item currently treated
        objs_indx = [ x-1 for x in objs_indx]       # update new values of indeces
        if i!=0 and areAllFunctions(exp[i-1]):      # classical case: resolve with the antecedent
            temp = resolve(exp[i], exp[i-1])        # get application
            exp[i-1] = temp                         # update list of expressions
            exp.pop(i)                              # remove resolvent
        elif (i==0 and areAllFunctions(exp[i+1])) or (i!=0 and areAllObjects(exp[i-1]) and areAllFunctions(exp[i+1])):      # whether the previous item not available while next is
            temp = resolve(exp[i], exp[i+1])
            exp[i+1] = temp
            exp.pop(i)
        else:
            continue
    return exp


# --- Recursive function that produces the final LTLf formula. ---
# :param exp: list of lambda expressions to be reduced
# :return exp: final lambda expression

def beta_reduction(exp):
    
    if len(exp)==1 and areAllObjects(exp):          # BASE CASE: correct
        return exp
    elif len(exp)>1 and areAllObjects(exp):
        print('+++ Not enough functions for objects in expression: ', exp)  # BASE CASE: wrong (too many objects)
        sys.exit()
    elif areAllFunctions(exp):
        print('+++ Not enough objects for functions in expression: ', exp)  # BASE CASE: wrong (too many functions)
        sys.exit()
    else:                                           # RECURSION
        exp = iter_step(exp)                        
        exp = beta_reduction(exp)                   # recursive call


# --- Function that perdforms the grounding of the formula. ---
# :param formula: final lambda expression
# :return formula: LTLf grounded formula

def ground(formula):
    
    formula = formula.replace(',', '_')
    reg = '[a-z]+\([a-z_]+\)'
    occ = re.findall(reg, formula)      # find occurrences of the RE reg in the formula
    for elem in occ:
        formula = formula.replace(elem, elem.replace('(', '_').replace(')', ''))
    return formula


# --- Function that translates a sentences into an LTLf formula according to a mapping. ---
# :param sent: sentence to be translated
# :param path: path of the mapping .csv file
# :return lambda_exp: LTLf expression that is the translation of the sentence

def NL2LTL(sent, path):

    mapping = load_mapping(path)                            # load rules
    comp = get_composite(mapping)                           # get list of all items made by more than one word
    sent = sent.lower()                                     # to lower sentence
    for elem in comp:
        sent = sent.replace(elem, elem.replace(' ', '_'))   # fix phrasal verbs
    tokenizer = nltk.RegexpTokenizer(r"\w+")
    sent = tokenizer.tokenize(sent)                         # remove punctuation and tokenize sentence
    lambda_exp = get_lambda_TL_list(sent, mapping)          # get list lambda expressions
    beta_reduction(lambda_exp)                              # recursive function
    lambda_exp = lambda_exp[0][1]                           # get formula
    lambda_exp = ground(lambda_exp)                         # ground the formula
    return lambda_exp



if __name__ == "__main__":
    
    i = time.time()
    sent = args.sentence
    path = args.path
    #sent = 'Go to the breakroom and report the location of the blue box!'
    #sent = 'When detect the blue box do not report the position of the recharge station'
    LTLf = NL2LTL(sent, path)
    print('Translated: ', sent)
    print('Into: ', LTLf)
    f = time.time()
    print('Time elapsed: ', f-i, 's')