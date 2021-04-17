#define CHECK_EOF if (file_input.eof()) return
#define SET_NEXT_CHAR file_input.get(cur_ch)

#include <jni.h>
#include "include/com_example_journal_PageParser.h"

#include <fstream>
#include <chrono>

#include <set>
#include <string>
#include <vector>

std::ifstream file_input;
std::ofstream file_output;

std::vector<std::string> _lessons;
std::vector<std::string> _marks;
std::vector<std::string> _hometasks;

std::set<std::string> _unique_lessons;

void parse_page()
{
	std::string tag = "", temp;
	char cur_ch;
	while (true)
	{
		SET_NEXT_CHAR;
		CHECK_EOF;

		if (cur_ch == '<')
		{
			for (tag = ""; cur_ch != '>'; SET_NEXT_CHAR) tag += cur_ch;
			tag += cur_ch;

			if (tag == "<td class=\"lesson \">" || tag == "<td class=\"lesson lesson_secondary\">")
			{
				tag = "";
				temp = "";
				bool previous_is_space = false;
				bool open_bracket_is_seen = false;
				while (true)
				{
					SET_NEXT_CHAR;
					CHECK_EOF;

					if (temp == "")
					{
						while (cur_ch == ' ' || cur_ch == '\n')
						{
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (cur_ch == ' ')
					{
						if (previous_is_space)
						{
							while (cur_ch == ' ' || cur_ch == '\n')
							{
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							previous_is_space = false;
						}
						else
						{
							temp += " ";
							previous_is_space = true;
						}
					}

					if (cur_ch == '<')
					{
						if (open_bracket_is_seen)
						{
							_lessons.push_back((temp.size() > 4 ? temp : "-")); // "1. " - temp.size() == 3
							temp = "";
							break;
						}
						else
						{
							open_bracket_is_seen = true;
							while (cur_ch != '>')
							{
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (cur_ch != ' ')
					{
						previous_is_space = false;
						temp += cur_ch;
					}
				}
				temp = "";
			}
			else if (tag == "<div class=\"mark_box \">")
			{
				tag = "";
				temp = "";
				while (true)
				{
					SET_NEXT_CHAR;
					CHECK_EOF;

					if (temp == "")
					{
						while (cur_ch == ' ')
						{
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}

					if (cur_ch == '<')
					{
						SET_NEXT_CHAR;
						CHECK_EOF;
						if (cur_ch == 's')
						{
							while (cur_ch != '>')
							{
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
							while (cur_ch != '<')
							{
								temp += cur_ch;
								SET_NEXT_CHAR;
								CHECK_EOF;
							}
							_marks.push_back(temp == "" ? "N/A" : temp);
							temp = "";
							break;
						}
						else
						{
							_marks.push_back("N/A");
							temp = "";
							break;
						}
					}
				}
			}
			else if (tag == "<td class=\"ht\">")
			{
				while (true)
				{
					SET_NEXT_CHAR;
					CHECK_EOF;
					if (cur_ch == '<')
					{
						for (tag = ""; cur_ch != '>'; SET_NEXT_CHAR) tag += cur_ch;
						tag += cur_ch;
						SET_NEXT_CHAR;
						CHECK_EOF;
					}

					if (tag == "<div class=\"ht-text\">")
					{
						while (cur_ch == ' ' || cur_ch == '\n')
						{
							SET_NEXT_CHAR;
							CHECK_EOF;
						}

						std::string ht_text = "";
						while (true)
						{
							if (cur_ch == '<')
							{
								for (tag = ""; cur_ch != '>'; SET_NEXT_CHAR) tag += cur_ch;
								tag += cur_ch;

								if (tag == "</div>")
								{
									_hometasks.push_back(ht_text);
									goto end_of_ht_reading_cycle;
								}
							}
							else
							{
								ht_text += cur_ch;
							}
							SET_NEXT_CHAR;
							CHECK_EOF;
						}
					}
					else if (tag == "</td>")
					{
						_hometasks.push_back("");
						break;
					}

					tag = "";
				}
				end_of_ht_reading_cycle:
				bool var_for_marker;
			}
		}
	}
}

void format_lessons()
{
	std::string res;
	for (int i = 0; i < _lessons.size(); i++)
	{
		if (_lessons[i] == "-") continue;
		res = "";

		int pos = 0;
		bool previous_is_space = false;
		bool previous_is_new_line = false;

		while (_lessons[i][pos] != ' ') pos++;
		pos++;

		while (pos < _lessons[i].size())
		{
			if (_lessons[i][pos] == ' ')
			{
				previous_is_space = true;
			}
			else if (_lessons[i][pos] == '\n')
			{
				previous_is_new_line = true;
			}
			else
			{
				if (previous_is_new_line)
				{
					previous_is_space = false;
					previous_is_new_line = false;
					res += "\n";
				}
				else if (!previous_is_new_line && previous_is_space)
				{
					previous_is_space = false;
					previous_is_new_line = false;
					res += " ";
				}
				res += _lessons[i][pos];
			}
			pos++;
		}

		_lessons[i] = res;
		_unique_lessons.insert(_lessons[i]);

		while (_hometasks[i][_hometasks[i].size() - 1] == ' ' || _hometasks[i][_hometasks[i].size() - 1] == '\n')
		{
			_hometasks[i].pop_back();
		}
	}
}

void fout_to_file()
{
	std::string output = "";
	int max_lessons = _lessons.size() / 6;
	for (int i = 0; i < 6; i++)
	{
		for (int j = 0; j < max_lessons; j++)
		{
			int index = i * max_lessons + j;
			output += _lessons[index];
			output += ">";
			output += _marks[index];
			output += ">";
			if (_hometasks.size() > 0) output += _hometasks[index];
			output += ">";
		}
	}
	file_output << output;
}

std::string jstring2string(JNIEnv *env, jstring jStr)
{
	if (!jStr) return "";

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
Java_com_example_journal_ParserPage_parsePage(JNIEnv *env, jobject thisObj, jstring page_path, jstring file_path)
{
	_lessons.clear();
	_marks.clear();
	_hometasks.clear();
	_unique_lessons.clear();

	file_input.open(jstring2string(env, page_path));
	file_output.open(jstring2string(env, file_path), std::ios_base::trunc);

	parse_page();
	format_lessons();
	fout_to_file();

	file_input.close();
	file_output.close();

	return;
}