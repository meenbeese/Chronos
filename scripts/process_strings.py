import xml.etree.ElementTree as ET
from pathlib import Path
from copy import deepcopy

source_xml = r"C:\Users\kuzey\GitHub\Chronos\app\src\main\res\values\strings.xml"
res_dir = Path(r"C:\Users\kuzey\GitHub\Chronos\app\src\main\res")
base_xml = Path(source_xml)

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

base_order = (
    translatable_false +
    normal_strings +
    plurals +
    string_arrays
)

def write_strings(path, existing_map):
    with open(path, "w", encoding="utf-8") as f:
        f.write("<resources>\n")

        for elem in base_order:
            key = (elem.tag, elem.attrib.get("name"))
            out = deepcopy(existing_map.get(key, elem))

            attribs = " ".join(f'{k}="{v}"' for k, v in out.attrib.items())

            if out.tag == "string":
                f.write(
                    f'    <string {attribs}>{escape_text(out.text)}</string>\n'
                )

            elif out.tag == "plurals":
                f.write(f'    <plurals {attribs}>\n')
                for item in out.findall("item"):
                    qty = item.attrib.get("quantity", "")
                    text = escape_text(item.text)
                    f.write(f'        <item quantity="{qty}">{text}</item>\n')
                f.write(f'    </plurals>\n')

            elif out.tag == "string-array":
                f.write(f'    <string-array {attribs}>\n')
                for item in out.findall("item"):
                    text = escape_text(item.text)
                    f.write(f'        <item>{text}</item>\n')
                f.write(f'    </string-array>\n')

        f.write("</resources>\n")

write_strings(
    base_xml,
    {(e.tag, e.attrib.get("name")): e for e in root}
)

for values_dir in res_dir.glob("values-*"):
    target_xml = values_dir / "strings.xml"
    if not target_xml.exists():
        continue

    tree = ET.parse(target_xml)
    root = tree.getroot()

    existing = {
        (e.tag, e.attrib.get("name")): e
        for e in root
    }

    write_strings(target_xml, existing)

