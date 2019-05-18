import requests
from bs4 import BeautifulSoup

trec_file = open("wiki_pages.txt", "w+")
id = ''
counter = 0

for i in range(1000):
    r = requests.get("http://en.wikipedia.org/wiki/Special:Random")
    counter += 1
    if counter % 10 == 0:
        print(counter)
    trec_file.write("<DOC>\n")
    trec_file.write("<DOCNO>"+str(counter)+"</DOCNO>\n")
    soup = BeautifulSoup(r.content, 'html.parser')
    trec_file.write("<TITLE>"+soup.find('title').get_text()+"</TITLE>\n")
    trec_file.write("<TEXT>")
    for j in soup.find_all('p'):
        trec_file.write(j.get_text())
    trec_file.write("</TEXT>\n")
    trec_file.write("</DOC>\n")
