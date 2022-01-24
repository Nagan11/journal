#define CHECK_EOF if (fileInputStream.eof()) goto endReadingFileMarker
#define SET_NEXT_CHAR fileInputStream.get(currentChar)


#include <jni.h>
#include "include/com_example_journal_parsers_LastPageParser.h"

#include <fstream>

#include <vector>


using namespace std;


ifstream fileInputStream;
ofstream fileOutputStream;

vector<string> _lessons;
vector<vector<string>> _marks;

void parsePage() {
	char currentChar;
	string tag, temp;
	while (true) {
		SET_NEXT_CHAR;
		CHECK_EOF;

		if (currentChar == '<') {
			for (tag = ""; currentChar != '>'; SET_NEXT_CHAR) {
				tag += currentChar;
			}
			tag += currentChar;

			if (tag == "<td class=\"ttl\">") { // read lesson name
				while (true) {
					SET_NEXT_CHAR;
					if (currentChar == '<') {
						SET_NEXT_CHAR;
						if (currentChar == 'a' || currentChar == 'b') {
							break;
						}
					}
					CHECK_EOF;
				}

				if (currentChar != 'b') {
					while (currentChar != '>') {
						SET_NEXT_CHAR;
					}
					SET_NEXT_CHAR;

					while (currentChar == ' ' || currentChar == '\t' || currentChar == '\n') {
						SET_NEXT_CHAR;
					}

					for (temp = ""; currentChar != '<'; SET_NEXT_CHAR) {
						temp += currentChar;
					}

					while (temp.back() == ' ' || temp.back() == '\t' || temp.back() == '\n') {
						temp.pop_back();
					}

					_lessons.push_back(temp);
				}
			}

			if (tag == "<tr class=\"marks\">") { // read quarter marks
				vector<string> lessonMarks;
				for (int i = 0; i < 5; i++) {
					temp = "";
					SET_NEXT_CHAR;
					while (temp != "<td class=\"qmark\">" && temp != "<td class=\"ymark\">") {
						while (currentChar != '<') {
							SET_NEXT_CHAR;
						}
						for (temp = ""; currentChar != '>'; SET_NEXT_CHAR) {
							temp += currentChar;
						}
						temp += currentChar;
					}

					temp = "";
					SET_NEXT_CHAR;
					while (currentChar != '<') {
						temp += currentChar;
						SET_NEXT_CHAR;
					}
					lessonMarks.push_back(temp.size() == 0 ? "-" : temp);
				}
				_marks.push_back(lessonMarks);
			}
		}
	}

	endReadingFileMarker:
	bool varForMarker;
}

void printToFile() {
	string output = "";
	for (int i = 0; i < _lessons.size(); i++) {
		output += _lessons[i];
		output += ">";
		for (int j = 0; j < 5; j++) {
			output += _marks[i][j];
			output += ">";
		}
	}
	fileOutputStream << output;
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
Java_com_example_journal_parsers_LastPageParser_parsePage
(JNIEnv *env, jobject thisObj, jstring pagePath, jstring filePath) {
	_lessons.clear();
	_marks.clear();

	fileInputStream.open(jstring2string(env, pagePath));
	fileOutputStream.open(jstring2string(env, filePath), ios_base::trunc);

	parsePage();
	printToFile();

	fileInputStream.close();
	fileOutputStream.close();

	return;
}