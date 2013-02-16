FileSyncTool
============

Installation:

Match the top level directories (src and tools) to their corresponding directories under the javasrc directory in your ccnx package and copy the contents.  filesync.java should sit with the rest of the java utils under org.ccnx.ccn.utils and filesync shoud sit with the rest of the shell scripts under tools.  

Recompile and reinstall ccnx on your machine; this filesync tool will be installed and will function like any of the other tools provided by ccnx.

Usage:

Creating a Slice:

Specify the slice argument and give a topology and naming prefix:

filesync -slice <topo> <prefix>

Put a new snapshot:

Specify the putSnapshot argument, along with the naming prefix you defined in the slice:

filesync -putSnapshot <prefix>

This tool will read from stdin the contents that should be placed in the snapshot, until it sees a new line character:

ccnx:/local/ndnslide/lecture1.pdf ccnx:/local/ndnslide/lecture2.pdf\n

Get the latest snapshot:

Specify the getLatestSnapshot arguemnt, along with the naming prefix you defined in the slice:

filesync -getLatestSnapshot <prefix>

This tool will retrieve the latest snapshot and write its contents to stdout.

Other Notes:

This tool uses the CCNx Synchronization Protocol to maintain snapshot consistency among peers.


