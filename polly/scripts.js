var API_ENDPOINT = "https://6hj1ociwy1.execute-api.ap-northeast-2.amazonaws.com/dev"
if (API_ENDPOINT === "")
{
        alert("API Gateway에 배포한 URL을 등록하고 실행하세요.");
}

document.getElementById("sayButton").onclick = function(){
        document.getElementById("postIDreturned").textContent="게시물 등록: 요청중";
        var inputData = {
                "voice": $('#voiceSelected option:selected').val(),
                "text" : $('#postText').val(),
                "hanja": $('#hanja').is(":checked")
        };

        $.ajax({
                url: API_ENDPOINT,
                type: 'POST',
                data:  JSON.stringify(inputData),
                contentType: 'application/json; charset=utf-8',
                success: function (response) {
                        document.getElementById("postIDreturned").textContent="게시물 등록 번호: " + response;
                },
                error: function () {
                        alert("에러가 발생했습니다. 확인해 주세요.");
                }
        });
}


document.getElementById("searchButton").onclick = function(){

        var postId = $('#postId').val();

        $.ajax({
                url: API_ENDPOINT + '?postId='+postId,
                type: 'GET',
                success: function (response) {
                        $('#posts tr').slice(1).remove();
                        jQuery.each(response, function(i,data) {
                                var player = "<audio controls><source src='" + data['mp3Url'] + "' type='audio/mpeg'></audio>"
                                if (typeof data['mp3Url'] === "undefined") {
                                        var player = ""
                                }
                                $("#posts").append("<tr> \
                                        <td>" + data['id'] + "</td> \
                                        <td>" + data['replaceText'] + "</td> \
                                        <td>" + data['status'] + "</td> \
                                        <td>" + player + "</td> \
                                        </tr>");
                });
                        },
                        error: function () {
                                alert("error");
                        }
        });
}

document.getElementById("postText").onkeyup = function(){
        var length = $(postText).val().length;
        document.getElementById("charCounter").textContent="Characters: " + length;
}