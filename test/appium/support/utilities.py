def fill_string_with_char(string: str, fillchar: str, amount: int, start: bool = False, end: bool = False) -> str:
    """
    :param string: input string to be formatted
    :param fillchar: character to fill the original string with
    :param amount: how many fillchar characters to put
    :param start: allows to put fillchar at the beginning of the original string
    :param end: allows to put fillchar at the ending of the original string
    :return: new string formatted

    usage:

    fill_string_with_char(string="club script position", fillchar='*', amount=3)
    output will be: 'club***script***position'

    fill_string_with_char(string="club script position", fillchar='*', amount=3, start=True, end=True)
    output will be: '***club***script***position***'

    """
    fill_with = fillchar * amount
    string_revised = fill_with.join(string.split())
    if start:
        string_revised = string_revised.rjust(len(string_revised)+len(fill_with), fillchar)
    if end:
        string_revised = string_revised.ljust(len(string_revised)+len(fill_with), fillchar)
    return string_revised
