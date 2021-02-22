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
	char current_char;
	while (true)
	{
		file_input.get(current_char);
		if (file_input.eof()) return;

		if (current_char == '<')
		{
			// read tag
			tag = "";
			while (current_char != '>')
			{
				tag += current_char;
				file_input.get(current_char);
				if (file_input.eof()) return;
			}
			tag += current_char;

			if (tag == "<td class=\"lesson \">" || tag == "<td class=\"lesson lesson_secondary\">")
			{
				tag = "";
				temp = "";
				bool previous_is_space = false;
				bool open_bracket_is_seen = false;
				while (true)
				{
					file_input.get(current_char);
					if (file_input.eof()) return;

					if (temp == "")
					{
						while (current_char == ' ' || current_char == '\n')
						{
							file_input.get(current_char);
							if (file_input.eof()) return;
						}
					}

					if (current_char == ' ')
					{
						if (previous_is_space)
						{
							while (current_char == ' ' || current_char == '\n')
							{
								file_input.get(current_char);
								if (file_input.eof()) return;
							}
							previous_is_space = false;
						}
						else
						{
							temp += " ";
							previous_is_space = true;
						}
					}

					if (current_char == '<')
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
							while (current_char != '>')
							{
								file_input.get(current_char);
								if (file_input.eof()) return;
							}
							file_input.get(current_char);
							if (file_input.eof()) return;
						}
					}

					if (current_char != ' ')
					{
						previous_is_space = false;
						temp += current_char;
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
					file_input.get(current_char);
					if (file_input.eof()) return;

					if (temp == "")
					{
						while (current_char == ' ')
						{
							file_input.get(current_char);
							if (file_input.eof()) return;
						}
					}

					if (current_char == '<')
					{
						file_input.get(current_char);
						if (file_input.eof()) return;
						if (current_char == 's')
						{
							while (current_char != '>')
							{
								file_input.get(current_char);
								if (file_input.eof()) return;
							}
							file_input.get(current_char);
							if (file_input.eof()) return;
							while (current_char != '<')
							{
								temp += current_char;
								file_input.get(current_char);
								if (file_input.eof()) return;
							}
							if (temp == "")
							{
								_marks.push_back("N/A");
							}
							else
							{
								_marks.push_back(temp);
							}
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
					file_input.get(current_char);
					if (file_input.eof()) return;
					if (current_char == '<')
					{
						// read tag
						tag = "";
						while (current_char != '>')
						{
							tag += current_char;
							file_input.get(current_char);
							if (file_input.eof()) return;
						}
						tag += current_char;
						file_input.get(current_char);
						if (file_input.eof()) return;
					}

					if (tag == "<div class=\"ht-text\">")
					{
						while (current_char == ' ' || current_char == '\n')
						{
							file_input.get(current_char);
							if (file_input.eof()) return;
						}

						std::string ht_text = "";
						while (true)
						{
							if (current_char == '<')
							{
								// read tag
								tag = "";
								while (current_char != '>')
								{
									tag += current_char;
									file_input.get(current_char);
									if (file_input.eof()) return;
								}
								tag += current_char;

								if (tag == "</div>")
								{
									_hometasks.push_back(ht_text);
									goto end_of_ht_reading_cycle;
								}
							}
							else
							{
								ht_text += current_char;
							}
							file_input.get(current_char);
							if (file_input.eof()) return;
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
		if (_lessons[i] == "-")
		{
			continue;
		}
		res = "";

		int pos = 0;
		bool previous_is_space = false;
		bool previous_is_new_line = false;

		while (_lessons[i][pos] != ' ')
		{
			pos++;
		}
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
			int temp = i * max_lessons + j;
			output += _lessons[temp];
			output += ">";
			output += _marks[temp];
			output += ">";
			if (_hometasks.size() > 0)
			{
				output += _hometasks[temp];
			}
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
Java_com_example_journal_PageParser_parsePage(JNIEnv *env, jobject thisObj, jstring page_path, jstring file_path)
{
	std::chrono::time_point<std::chrono::steady_clock> start = std::chrono::steady_clock::now();

	_lessons.clear();
	_marks.clear();
	_hometasks.clear();
	_unique_lessons.clear();

	file_input.open(jstring2string(env, page_path));
	file_output.open(jstring2string(env, file_path), std::ios_base::trunc);

	parse_page();
	format_lessons();
	fout_to_file();

	std::chrono::time_point<std::chrono::steady_clock> finish = std::chrono::steady_clock::now();
	std::chrono::milliseconds elapsed_time = std::chrono::duration_cast<std::chrono::milliseconds>(finish - start);

	file_output << "\n" << elapsed_time.count();

	file_input.close();
	file_output.close();

	return;
}

















