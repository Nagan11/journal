#define CHECK_EOF if (file_input.eof()) goto end_reading_file
#define SET_NEXT_CHAR file_input.get(cur_ch)

#include <jni.h>
#include "include/com_example_journal_ParserLastPage.h"

#include <fstream>

#include <vector>

std::ifstream file_input;
std::ofstream file_output;

std::vector<std::string> _lessons;
std::vector< std::vector<std::string> > _marks;

void parse_page()
{
	char cur_ch;
	std::string tag, temp;
	while (true)
	{
		SET_NEXT_CHAR;
		CHECK_EOF;

		if (cur_ch == '<')
		{
			for (tag = ""; cur_ch != '>'; SET_NEXT_CHAR) tag += cur_ch;
			tag += cur_ch;

			if (tag == "<td class=\"ttl\">") // read lesson name
			{
				while (true)
				{
					SET_NEXT_CHAR;
					if (cur_ch == '<')
					{
						SET_NEXT_CHAR;
						if (cur_ch == 'a' || cur_ch == 'b') break;
					}
					CHECK_EOF;
				}

				if (cur_ch != 'b')
				{
					while (cur_ch != '>') SET_NEXT_CHAR;
					SET_NEXT_CHAR;

					while (cur_ch == ' '  ||
						   cur_ch == '\t' ||
						   cur_ch == '\n') SET_NEXT_CHAR;

					for (temp = ""; cur_ch != '<'; SET_NEXT_CHAR) temp += cur_ch;

					while (temp[temp.size() - 1] == ' '  ||
						   temp[temp.size() - 1] == '\t' ||
						   temp[temp.size() - 1] == '\n') temp.pop_back();

					_lessons.push_back(temp);
				}
			}

			if (tag == "<tr class=\"marks\">") // read quarter marks
			{
				std::vector<std::string> lesson_marks;
				for (int i = 0; i < 5; i++)
				{
					temp = "";
					SET_NEXT_CHAR;
					while (temp != "<td class=\"qmark\">" && temp != "<td class=\"ymark\">")
					{
						while (cur_ch != '<') SET_NEXT_CHAR;
						for (temp = ""; cur_ch != '>'; SET_NEXT_CHAR) temp += cur_ch;
						temp += cur_ch;
					}

					temp = "";
					SET_NEXT_CHAR;
					while (cur_ch != '<')
					{
						temp += cur_ch;
						SET_NEXT_CHAR;
					}
					lesson_marks.push_back(temp.size() == 0 ? "-" : temp);
				}
				_marks.push_back(lesson_marks);
			}
		}
	}

	end_reading_file:
	bool var_for_marker;
}

void fout_to_file()
{
	std::string output = "";
	for (int i = 0; i < _lessons.size(); i++)
	{
		output += _lessons[i];
		output += ">";
		for (int j = 0; j < 5; j++)
		{
			output += _marks[i][j];
			output += ">";
		}
	}
	file_output << output;
}

std::string jstring2string(JNIEnv *env, jstring jStr)
{
	if (!jStr)
	{
		return "";
	}

	const jclass stringClass = env->GetObjectClass(jStr);
	const jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
	const jbyteArray stringJbytes = (jbyteArray)env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

	size_t length = (size_t)env->GetArrayLength(stringJbytes);
	jbyte* pBytes = env->GetByteArrayElements(stringJbytes, NULL);

	std::string ret = std::string((char*)pBytes, length);
	env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

	env->DeleteLocalRef(stringJbytes);
	env->DeleteLocalRef(stringClass);
	return ret;
}

JNIEXPORT void JNICALL 
Java_com_example_journal_ParserLastPage_parsePage(JNIEnv *env, jobject thisObj, jstring page_path, jstring file_path)
{
	_lessons.clear();
	_marks.clear();

	file_input.open(jstring2string(env, page_path));
	file_output.open(jstring2string(env, file_path), std::ios_base::trunc);

	parse_page();
	fout_to_file();

	file_input.close();
	file_output.close();

	return;
}