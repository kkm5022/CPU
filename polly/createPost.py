import boto3 
import os 
import json 
import uuid 
import datetime 

def lambda_handler(event, context): 
 
     recordId = str(uuid.uuid4())     
     voice = event["voice"]
     originText = event["text"]
     hanja = event["hanja"]
     updateDate = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
     
     print('Generating new DynamoDB record, with ID: ' + recordId)
     print('Input Text: ' + originText)
     print('Selected voice: ' + voice)
     
       # Hanja to Korean
     if hanja:
         lambda_client = boto3.client('lambda')
         invoke_response = lambda_client.invoke(
             FunctionName = "HanjaToKorean",
             InvocationType = 'RequestResponse',
             Payload = json.dumps({"inputText": originText})
         )
         data = invoke_response['Payload'].read()
         resultText = json.loads(data)
         replaceText = resultText['outputText']
         print('Hanja to Korean Text: ' + replaceText)
     else:
         replaceText = originText
         
     # Creating new record in DynamoDB table
     dynamodb = boto3.resource('dynamodb')
     table = dynamodb.Table(os.environ['DB_TABLE_NAME'])
     table.put_item(
         Item={
             'id' : recordId,
             'voice' : voice,
             'text': originText,
             'replaceText': replaceText,
             'status' : "PROCESSING",
             'updateDate': updateDate
         }
     )       
     # Sending notification about new post to SNS
     client = boto3.client('sns')
     client.publish(
         TopicArn = os.environ['SNS_TOPIC'],
         Message = recordId
     )
     return recordId