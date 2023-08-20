import io
from io import BytesIO
from typing import BinaryIO

from PyPDF2 import PdfReader, PdfWriter


def extract_page(input_pdf: str | BinaryIO, page_number: int) -> BytesIO:
    reader = PdfReader(open(input_pdf, "rb"))
    if page_number >= len(reader.pages):
        raise ValueError(f"Invalid page number '{page_number}'.")

    # temp_name = f"{input_pdf.replace('.pdf', '')}_Seite_{page_number + 1}.pdf"
    buffer = io.BytesIO()
    output = PdfWriter()
    output.add_page(reader.pages[page_number])
    output.write(buffer)
    return buffer


def replace_page(
    input_pdf: str,
    input_page_number: int,
    replacement_pdf: str,
    replacement_page_number: int,
    output_file: str = "out.pdf"
) -> None:
    reader_input = PdfReader(open(input_pdf, "rb"))
    reader_replacement = PdfReader(open(replacement_pdf, "rb"))

    output = PdfWriter()
    for i in range(reader_input.numPages):
        if i == input_page_number:
            output.add_page(reader_replacement.getPage(replacement_page_number))
        else:
            output.add_page(reader_input.getPage(i))

    with open(output_file, "wb") as outputStream:
        output.write(outputStream)


def replace_pages(
    input_pdf: str, input_page_numbers: list[int], replacement_pdf: str, replacement_page_numbers: list[int], output_file: str = "out.pdf"
) -> None:
    reader_input = PdfReader(open(input_pdf, "rb"))
    reader_replacement = PdfReader(open(replacement_pdf, "rb"))

    output = PdfWriter()
    for i in range(reader_input.numPages):
        if i in input_page_numbers:
            output.add_page(reader_replacement.getPage(replacement_page_numbers[input_page_numbers.index(i)]))
        else:
            output.add_page(reader_input.getPage(i))

    with open(output_file, "wb") as outputStream:
        output.write(outputStream)


def merge_pdfs(pdf_files: list[str]) -> BytesIO:
    output = PdfWriter()
    print(pdf_files)
    for pdf_file in pdf_files:
        try:
            reader = PdfReader(open(pdf_file, "rb"))
            for page in reader.pages:
                output.add_page(page)
        except FileNotFoundError:
            continue

    buffer = io.BytesIO()
    output.write(buffer)
    return buffer


def insert_pdf(target_pdf: str, pdf_to_insert: str, insert_after_page: int = -1, output_filename: str = "out.pdf") -> None:
    reader_target = PdfReader(open(target_pdf, "rb"))
    reader_insert = PdfReader(open(pdf_to_insert, "rb"))

    if insert_after_page >= reader_target.numPages:
        raise ValueError("Page number too high")

    if insert_after_page == -1:
        insert_after_page = reader_target.numPages - 1

    stop_idx = 0

    output = PdfWriter()

    for idx, page in enumerate(reader_target.pages):
        if idx < insert_after_page:
            output.add_page(page)
            stop_idx += 1

    for page in reader_insert.pages:
        output.add_page(page)

    if stop_idx != reader_target.numPages - 1:
        for page in reader_target.pages[stop_idx:]:
            output.add_page(page)

    with open(output_filename, "wb") as outputStream:
        output.write(outputStream)


if __name__ == "__main__":
    replace_page("2023-04-27_ID Logistics Kaiserslautern_Energieauditerklärung.pdf", 1, "Signed.pdf", 0, "output.pdf")
