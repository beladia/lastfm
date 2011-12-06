// lastfm.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include "../../../Snap/snap/Snap.h"
#include <fstream>
#include <iostream>
#include <string>
#include <vector>
#include <sstream>
using namespace std;

void split(const string& s, char c,
           vector<string>& v) {
   string::size_type i = 0;
   string::size_type j = s.find(c);
   while (j != string::npos) {
      v.push_back(s.substr(i, j-i));
      i = ++j;
      j = s.find(c, j);
      if (j == string::npos)
         v.push_back(s.substr(i, s.length( )));
   }
}

int _tmain(int argc, _TCHAR* argv[])
{
	// read in the edgelist file into un-directed graph
	PUNGraph g = TSnap::LoadEdgeList<PUNGraph>("C:/Users/beladia/workspace/lastfm/datazips/newdata/EdgeList_US.txt.dat", 0, 1);



	// for every pair of nodes in the nodepair file
	//   calculate the shortest path between the nodes in graph g 
	
	typedef vector<vector<string> > Rows;
	Rows rows;
	ifstream input("C:/Users/beladia/workspace/lastfm/datazips/newdata/traindata_tags_us633.dat");
	ofstream outFile;
	outFile.open("C:/Users/beladia/workspace/lastfm/datazips/newdata/traindata_shortpath_us633.dat");

	char const row_delim = '\n';
	char const field_delim = '\t';
	int node1, node2;
	int i = 0;
	vector<string> v;

	for (string row; getline(input, row, row_delim); ) {
	  rows.push_back(Rows::value_type());

	  istringstream ss(row);
	  for (string field; getline(ss, field, field_delim); ) {
		rows.back().push_back(field);
	  }

	  split(rows.at(i).at(0).c_str(), '#', v);
	  node1 = atoi(v[0].c_str());
	  node2 = atoi(v[1].c_str());

	  outFile << rows.at(i).at(0).c_str() << "\t" << TSnap::GetShortPath(g, node1, node2) << "\n";

	  //printf("Shortest path (%d, %d) = %d \n", node1, node2, TSnap::GetShortPath(g, node1, node2));
	}

	input.close();
	outFile.close();

	getchar();

	return 0;
}

