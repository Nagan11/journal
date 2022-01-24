#define CHECK_EOF if (fileInput.eof()) return
#define SET_NEXT_CHAR fileInput.get(currentChar)


#include <jni.h>
#include "include/com_example_journal_parsers_WeekPageParser.h"

#include <fstream>

#include <set>
#include <string>
#include <vector>


using namespace std;


ifstream fileInput;
ofstream fileOutput;

vector<string> _lessons;
vector<string> _marks;
vector<string> _hometasks;

set<string> _uniqueLessons;

void parsePage() {
	string tag = "", temp;
	char currentChar;
	while (true) {
		SET_NEXT_CHAR;
		CHECK_EOF;

		if (currentChar == '<') {
			for (tag = ""; currentChar != '>'; SET_NEXT_CHAR) {
				tag += currentChar;
			}
			tag += currentChar;

			if (tag == "<td class=\"lesson \">" || tag == "<td class=\"lesson lesson_secondary\">") {
				tag = "";
				temp = "";
				bool previousIsSpace = false;
				bool openBracketIsSeen = false;
				while (true) {
					SET_NEXT_CHAR;
					CHECK_EOF;

					if (temp == "") {
						while (currentChar == ' ' || currentChar == '\n') {
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (currentChar == ' ') {
						if (previousIsSpace) {
							while (currentChar == ' ' || currentChar == '\n') {
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							previousIsSpace = false;
						} else {
							temp += " ";
							previousIsSpace = true;
						}
					}

					if (currentChar == '<') {
						if (openBracketIsSeen) {
							_lessons.push_back((temp.size() > 4 ? temp : "-")); // "1. " - temp.size() == 3
							temp = "";
							break;
						} else {
							openBracketIsSeen = true;
							while (currentChar != '>') {
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (currentChar != ' ') {
						previousIsSpace = false;
						temp += currentChar;
					}
				}
				temp = "";
			} else if (tag == "<div class=\"mark_box \">") {
				tag = "";
				temp = "";
				while (true) {
					SET_NEXT_CHAR;
					CHECK_EOF;

					if (temp == "") {
						while (currentChar == ' ') {
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (currentChar == '<') {
						SET_NEXT_CHAR;
						CHECK_EOF;
						if (currentChar == 's') {
							while (currentChar != '>') {
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
							while (currentChar != '<') {
								temp += currentChar;
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							_marks.push_back(temp == "" ? "N/A" : temp);
							temp = "";
						} else {
							_marks.push_back("N/A");
							temp = "";
						}
						break;
					}
				}
			} else if (tag == "<td class=\"ht\">") {
				while (true) {
					SET_NEXT_CHAR;
					CHECK_EOF;
					if (currentChar == '<') {
						for (tag = ""; currentChar != '>'; SET_NEXT_CHAR) {
							tag += currentChar;
						}
						tag += currentChar;
						SET_NEXT_CHAR;
						CHECK_EOF;
					}

					if (tag == "<div class=\"ht-text\">") {
						while (currentChar == ' ' || currentChar == '\n') {
							SET_NEXT_CHAR;
							CHECK_EOF;
						}

						string hometaskText = "";
						while (true) {
							if (currentChar == '<') {
								for (tag = ""; currentChar != '>'; SET_NEXT_CHAR) {
									tag += currentChar;
								}
								tag += currentChar;

								if (tag == "</div>") {
									_hometasks.push_back(hometaskText);
									goto endOfHometaskReadingCycleMarker;
								}
							} else {
								hometaskText += currentChar;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					} else if (tag == "</td>") {
						_hometasks.push_back("");
						break;
					}

					tag = "";
				}
				endOfHometaskReadingCycleMarker:
				bool varForMarker;
			}
		}
	}
}

void formatLessons() {
	for (int i = 0; i < _lessons.size(); i++) {
		if (_lessons[i] == "-") {
			continue;
		}
		string res = "";

		int pos = 0;
		bool previousIsSpace = false;
		bool previousIsNewLine = false;

		while (_lessons[i][pos] != ' ') {
			pos++;
		}
		pos++;

		while (pos < _lessons[i].size()) {
			if (_lessons[i][pos] == ' ') {
				previousIsSpace = true;
			} else if (_lessons[i][pos] == '\n') {
				previousIsNewLine = true;
			} else {
				if (previousIsNewLine) {
					previousIsSpace = false;
					previousIsNewLine = false;
					res += "\n";
				} else if (!previousIsNewLine && previousIsSpace) {
					previousIsSpace = false;
					previousIsNewLine = false;
					res += " ";
				}
				res += _lessons[i][pos];
			}
			pos++;
		}

		_lessons[i] = res;
		_uniqueLessons.insert(_lessons[i]);

		while (_hometasks[i].back() == ' ' || _hometasks[i].back() == '\n') {
			_hometasks[i].pop_back();
		}
	}
}

void printToFile() {
	string output = "";
	int maxLessons = _lessons.size() / 6;
	for (int i = 0; i < 6; i++) {
		for (int j = 0; j < maxLessons; j++) {
			int index = i * maxLessons + j;
			output += _lessons[index];
			output += ">";
			output += _marks[index];
			output += ">";
			if (_hometasks.size() > 0) {
				output += _hometasks[index];
			}
			output += ">";
		}
	}
	fileOutput << output;
}

string jstring2string(JNIEnv *env, jstring jStr) {
	if (!jStr) {
		return "";
	}

	const jclass stringClass = env->GetObjectClass(jStr);
	const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
	const jbyteArray stringJbytes = (jbyteArray)env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

	size_t length = (size_t)env->GetArrayLength(stringJbytes);
	jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

	string ret = string((char*)pBytes, length);
	env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

	env->DeleteLocalRef(stringJbytes);
	env->DeleteLocalRef(stringClass);
	return ret;
}

JNIEXPORT void JNICALL
Java_com_example_journal_parsers_WeekPageParser_parsePage
(JNIEnv *env, jobject thisObj, jstring pagePath, jstring filePath) {
	_lessons.clear();
	_marks.clear();
	_hometasks.clear();
	_uniqueLessons.clear();

	fileInput.open(jstring2string(env, pagePath));
	fileOutput.open(jstring2string(env, filePath), ios_base::trunc);

	parsePage();
	formatLessons();
	printToFile();

	fileInput.close();
	fileOutput.close();

	return;
}