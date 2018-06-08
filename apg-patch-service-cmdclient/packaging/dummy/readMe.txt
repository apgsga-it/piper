JHE (06.06.2018)
----------------
When installing apscli, we need a folder called /var/opt/apg-patch-cli. At installation time, this folder needs to be empty.
However, the ospackage plugin can't create empty folder, reason we we include a file called ".ignore".