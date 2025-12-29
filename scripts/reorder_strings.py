import xml.etree.ElementTree as ET

source_xml = r"C:\Users\kuzey\GitHub\Chronos\app\src\main\res\values\strings.xml"

tree = ET.parse(source_xml)
root = tree.getroot()

translatable_false = []
plurals = []
string_arrays = []
normal_strings = []

for elem in root:
    if elem.tag == "string":
        if elem.attrib.get("translatable", "true") == "false":
            translatable_false.append(elem)
        else:
            normal_strings.append(elem)
    elif elem.tag == "plurals":
        plurals.append(elem)
    elif elem.tag == "string-array":
        string_arrays.append(elem)
    else:
        normal_strings.append(elem)

def number_key(e):
    name = e.attrib.get("name", "")
    if name.startswith("number_"):
        mapping = {
            "zero": 0, "one": 1, "two": 2, "three": 3, "four": 4,
            "five": 5, "six": 6, "seven": 7, "eight": 8, "nine": 9
        }
        return mapping.get(name[7:], 999)
    return 999

def day_key(e):
    name = e.attrib.get("name", "")
    if name.startswith("day_") and name.endswith("_abbr"):
        order = ["sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday"]
        day_name = name[4:-5]
        if day_name in order:
            return order.index(day_name)
    return 999

def escape_text(text):
    if not text:
        return ""
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")

normal_strings.sort(key=lambda e: (number_key(e), day_key(e), e.attrib.get("name", "")))
translatable_false.sort(key=lambda e: (number_key(e), day_key(e), e.attrib.get("name", "")))
plurals.sort(key=lambda e: e.attrib.get("name", ""))
string_arrays.sort(key=lambda e: e.attrib.get("name", ""))

with open(source_xml, "w", encoding="utf-8") as f:
    f.write("<resources>\n")

    for block in (translatable_false, normal_strings):
        for elem in block:
            attribs = " ".join(f'{k}="{v}"' for k, v in elem.attrib.items())
            text = escape_text(elem.text)
            f.write(f'    <{elem.tag} {attribs}>{text}</{elem.tag}>\n')
        f.write("\n")

    for block in (plurals,):
        for elem in block:
            attribs = " ".join(f'{k}="{v}"' for k, v in elem.attrib.items())
            f.write(f'    <{elem.tag} {attribs}>\n')
            for item in elem.findall("item"):
                qty = item.attrib.get("quantity", "")
                text = escape_text(item.text)
                f.write(f'        <item quantity="{qty}">{text}</item>\n')
            f.write(f'    </{elem.tag}>\n\n')

    for block in (string_arrays,):
        for elem in block:
            attribs = " ".join(f'{k}="{v}"' for k, v in elem.attrib.items())
            f.write(f'    <{elem.tag} {attribs}>\n')
            for item in elem.findall("item"):
                text = escape_text(item.text)
                f.write(f'        <item>{text}</item>\n')
            f.write(f'    </{elem.tag}>\n')

    f.write("</resources>\n")

print(f"Strings reordered in-place: {source_xml}")
