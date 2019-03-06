eval "javac MyLH/*.java"
read -rsp $'Press enter to continue...\n'
eval "java MyLH.CreateStorageMain"
eval "java MyLH.ScanDataAndPopulateRelation"
read -rsp $'Press enter to continue...\n'
#eval "java MyLH.PrintRelation1"
#read -rsp $'Press enter to continue...\n'
#eval "java MyLH.ProcessRelation1"
#read -rsp $'Press enter to continue...\n'
#eval "java MyLH.PrintRelation2"
#read -rsp $'Press enter to continue...\n'
