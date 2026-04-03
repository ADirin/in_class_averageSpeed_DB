Activate the xmin:
PS C:\Program Files (x86)\xming> .\Xming.exe :0 -ac -multiwindow -clipboard

to run the minikube in intellij first make sure minikube has it own docker daemo

1. & minikube -p minikube docker-env --shell powershell | Invoke-Expression

build the image

2. docker build -t <your-image-name>:<tag> .
for example docker build -t amirdirin/sep2_week3_avgspd_db:latest .

Deploy to the kubernetes

3.  kubectl apply -f avgspeed_deployment.yaml
    kubectl get pods

 Ensure youu imagePullpolicy in the YAML is never
 4. imagePullPolicy: Never

 check the pods status become Running

 READY   STATUS    RESTARTS
 1/1     Running   0


 5. to delete the minikube
 kubectl delete pod <pod-name>
 kubectl delete pod avgspeed-app-5984c69657-958w6