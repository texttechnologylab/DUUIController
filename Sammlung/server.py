from fastapi import FastAPI, File, UploadFile
from fastapi.responses import Response

from pdf import extract_page, merge_pdfs

app = FastAPI()


@app.get("/pdf/extract")
async def extract_pdf_page(file: UploadFile = File(...), page: int = 0):
    pdf_page = extract_page(file.filename, page)

    return Response(pdf_page.getvalue(), headers={'Content-Disposition': 'inline; filename="out.pdf"'}, media_type='application/pdf')


@app.get("/pdf/merge")
async def merge_pdf_files(files: list[UploadFile] = File(...)):
    buffer = merge_pdfs(list(map(lambda file: file.filename, files)))
    return Response(buffer.getvalue(), headers={'Content-Disposition': 'inline; filename="out.pdf"'}, media_type='application/pdf')


@app.get("/")
async def home() -> dict:
    return {
        "Page": "Home"
    }
