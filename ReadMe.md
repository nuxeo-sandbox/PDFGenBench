
### Context

The goal of this project is to build a command line utility to generate a very large number of PDF to fill an AWS Snowball in the context of a large scale benchmark (10B documents).

### PDF generation

Because we need to generate 10B PDf:

 - generation time matters
 - file size matters

After doing several tests, the selecte approach is:

 - use iText PDF generation library
 - generate a template using iText
 - replace fixed size fields in the PDF

This is a very basic solution but allows to gerate up to 10K PDF/s on a laptop.

### Components

 - `PDFTemplateGenerator` used to generate the PDF used as template
 - `PDFFileGenerator` used to generate the final PDF file from the template and an array of variables
 - `BlobWriter` abstraction for writing the resulting PDF
 - `RandomDataGenerator` generate fake data using Random sequences and references data loaded from a CSV

### Build

   mvn clean package

### Command line

Run the exectable jar:

    java -jar target/pdf-gen-bench-1.0-SNAPSHOT.jar -h

Current command line options 

    -aws_key <arg>         AWS_ACCESS_KEY_ID
    -aws_secret <arg>      AWS_SECRET_ACCESS_KEY
    -aws_session <arg>     AWS_SESSION_TOKEN
    -h,--help              Help
    -m,--template <arg>    Template: 1 or 2 (default)
    -n,--nbThreads <arg>   Number of PDF to generate
    -o,--output <arg>      output: mem(default), tmp, file:<path>,
                           s3:<bucketName>, s3tm:<bucketName>,
                           s3tma:<bucketName>
    -t,--threads <arg>     Number of threads

Output options are:

 - `mem`: generate the PDFs purely in memory (no network or disk IO)
 - `tmp`: store the PDFs in a java temporary file
 - `file`: store the PDFs in the directory passed 
 - `s3`: store the PDFs in S3 using the std PUTObject API
 - `s3tm`: store the PDFs in S3 using the TransferManager
 - `s3tma`: store the PDFs in S3 using the TransferManager asynchronous API

NB: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `AWS_SESSION_TOKEN` can also be set as environment variables.

### Meta-data collections and logs

By *"MetaData collection"* we mean: persisting the data that were used to generate each PDF file

 - the metadata 
 - the digest (or BlobKey) for the pdf file 

The file `metadata.csv` is used to store these informations.

The file `injector.log` contains the log of the steps of the import.

### Example execution

Execution for 500,000 PDF files using template 2

    java -jar target/pdf-gen-bench-1.0-SNAPSHOT.jar -n 500000

Execution log

    Init Injector
      Threads:10
      pdfs:500000
    using template 2
    ----------------------------------------------------------
    0/500000 (0 d/s using 3 threads)
    149591/500000 (7480 d/s using 10 threads)
    326074/500000 (8152 d/s using 10 threads)

      Files: 500010 pdfs --- 8334 docs/s

      Projected generation time for 10B files: 
    13 day(s) and 21 hour(s)

The same execution on template 1 will be faster

    java -jar target/pdf-gen-bench-1.0-SNAPSHOT.jar -n 500000 -m 1


    Init Injector
      Threads:10
      pdfs:500000
    using template 1
    ----------------------------------------------------------
    ... 
    Files: 500010 pdfs --- 10417 docs/s

