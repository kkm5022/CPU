import boto3
import os
from contextlib import closing
from boto3.dynamodb.conditions import Key, Attr

def lambda_handler(event, context):
 
     postId = event["Records"][0]["Sns"]["Message"]
     print "Text to Speech function. Post ID in DynamoDB: " + postId     
    
     dynamodb = boto3.resource('dynamodb')
     table = dynamodb.Table(os.environ['DB_TABLE_NAME'])
     postItem = table.query(
         KeyConditionExpression=Key('id').eq(postId)
     )
     text = postItem["Items"][0]["replaceText"]     
     voice = postItem["Items"][0]["voice"]
     rest = text
     textBlocks = []
     while (len(rest) > 1100):
         begin = 0
         end = rest.find(".", 1000)
 
         if (end == -1): 
             end = rest.find(" ", 1000)

         textBlock = rest[begin:end]
         rest = rest[end:]
         textBlocks.append(textBlock)
     textBlocks.append(rest)
     
     polly = boto3.client('polly')
     for textBlock in textBlocks:
         response = polly.synthesize_speech(
             OutputFormat='mp3',
             Text = textBlock,
             VoiceId = voice
         ) 
      
         if "AudioStream" in response:
             with closing(response["AudioStream"]) as stream:
                 output = os.path.join("/tmp/", postId)
                 with open(output, "a") as file: 
                     file.write(stream.read()) 

     s3 = boto3.client('s3') 
     s3.upload_file('/tmp/' + postId,  
         os.environ['BUCKET_NAME'], 
         postId + ".mp3")
     s3.put_object_acl(ACL='public-read',
         Bucket=os.environ['BUCKET_NAME'],
         Key= postId + ".mp3") 

     location = s3.get_bucket_location(Bucket=os.environ['BUCKET_NAME'])     
     region = location['LocationConstraint']          

     if region is None:         
         url_begining = "https://s3.amazonaws.com/"
     else:
         url_begining = "https://s3-" + str(region) + ".amazonaws.com/"           
     url = url_begining  + str(os.environ['BUCKET_NAME']) + "/" + str(postId) + ".mp3"
 
     #Updating the item in DynamoDB     
     response = table.update_item(
         Key={'id':postId},
             UpdateExpression= 
                 "SET #statusAtt = :statusValue, #urlAtt = :urlValue",
             ExpressionAttributeValues=
                 {':statusValue': 'UPDATED', ':urlValue': url}, 
         ExpressionAttributeNames=
             {'#statusAtt': 'status', '#urlAtt': 'mp3Url'},
     )    
     
     return 