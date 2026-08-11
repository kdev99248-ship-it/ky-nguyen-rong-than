$(document).ready(function () {

    let homeUrl = "";

    getToken();

    function setToken(token, userName) {
        sessionStorage.setItem("token", token);
        sessionStorage.setItem("userName", userName);
    }

    function getToken() {
        let token = sessionStorage.getItem("token");
        if(token == null && (window.location.pathname == homeUrl + '/login'
            || window.location.pathname == homeUrl+ '/register'
            || window.location.pathname == homeUrl+ '/')) {
            return;
        }
        if (token == null && window.location.pathname != homeUrl + '/login') {
            window.location.href = homeUrl + "/login";
        }
        if (token != null && window.location.pathname == homeUrl + '/login') {
            window.location.href = homeUrl + "/home";
        }
        // set header title
        setHeaderTitle();
    }

    function setHeaderTitle() {
        $.ajax({
            url: homeUrl + "/api/user/profile",
            type: "GET",
            dataType: 'json', headers: {
                'Authorization': sessionStorage.getItem("token"),
            },
            contentType: "application/json",
            success: function (response) {
                $(".header-name").html("Xin chào: " + response.data.username + " - Có: " + response.data.point + " Xu | " + response.data.pointLock + " Xu Khóa");
                $("#turnRound").html( response.data.turnRound);
            }
        });
    }

    $("#register").click(function () {
        registerAPI();
    });

    // login function !!!
    function registerAPI() {
        let userName = $("#username").val();
        let password = $("#password").val();
        let email = $("#email").val();
        if(userName == null || password == null || email == null) {
            alert("Các thông tin bắt buộc !!!");
            return;
        }
        if(userName.trim().length < 5) {
            alert("Tài khoản > 6 kí tự !!!");
            return;
        }

        if(password.trim().length < 5) {
            alert("Mật khẩu  > 6 kí tự !!!");
            return;
        }
        let dataReg = {username : userName, password : password, email : email};

        $.ajax({
            url: homeUrl + "/api/user/register",
            type: "POST",
            dataType: 'json',
            contentType: "application/json",
            data: JSON.stringify(dataReg) ,
            success: function (response) {
                alert(response.msg);
                // setToken(response.data.token, response.data.userName);
                window.location.href = homeUrl + "/login";
            },
            error: function(jqXHR) {
                alert("Có lỗi xảy ra: "+ jqXHR.responseJSON.msg);
            }
        });
    }

    $("#login").click(function () {
        loginAPI();
    });

    // login function !!!
    function loginAPI() {
        let userName = $("#username").val();
        let password = $("#password").val();
        if(userName == null || password == null) {
            alert("Tài khoản hoặc mật khẩu không được để trống !!!");
            return;
        }
        if(userName.trim().length < 5) {
            alert("Tài khoản > 6 kí tự !!!");
            return;
        }

        if(password.trim().length < 5) {
            alert("Mật khẩu  > 6 kí tự !!!");
            return;
        }
        let dataLogin = {username : userName, password : password};

        $.ajax({
            url: homeUrl + "/api/login",
            type: "POST",
            dataType: 'json',
            contentType: "application/json",
            data: JSON.stringify(dataLogin) ,
            success: function (response) {
                alert(response.msg);
                setToken(response.data.token, response.data.userName);
                window.location.href = homeUrl + "/home";
            },
            error: function(jqXHR) {
                alert("Có lỗi xảy ra: "+ jqXHR.responseJSON.msg);
            }
        });
    }

    $("#checkAccount").click(function () {
        checkAccount();
    });

    function checkAccount() {
        let idInGame      = $("#idInGame").val();
        let idServer        = $("#idServer").val();
        let packageCharge   = $("#packageCharge").val();
        if(idInGame == null || idInGame.trim() == "") {
            alert("Vui lòng nhập id ingame!");
            return;
        }
        if(idServer == 0) {
            alert("Chọn server trước!");
            return;
        }
        let dataCheck = {idInGame : idInGame, idServer : idServer};
        $.ajax({
            url: homeUrl + "/api/user/check-account",
            type: "POST",
            dataType: 'json', headers: {
                'Authorization': sessionStorage.getItem("token"),
            },
            contentType: "application/json",
            data: JSON.stringify(dataCheck) ,
            success: function (response) {
                alert(response.msg);
            },
            error: function(jqXHR) {
                alert("Có lỗi xảy ra: "+ jqXHR.responseJSON.msg);
            }
        });
    }

    $("#buyPackage").click(function () {
        buyPackage();
    });

    function buyPackage() {
        let idInGame      = $("#playerName").val();
        let idServer        = $("#serverId").val();
        let packageCharge   = $("#packageCharge").val();
        let typePoint = $("#typePoint").val();
        if(idInGame == null || idInGame.trim() == "") {
            alert("Vui lòng nhập id ingame!");
            return;
        }
        if(idServer == 0) {
            alert("Chọn server trước!");
            return;
        }
        if (packageCharge == 0) {
            alert("Chọn gói nạp trước!");
            return;
        }
        let dataCharge = {idInGame: idInGame, idServer: idServer, packageCharge, packageCharge, typePoint: typePoint};
        $.ajax({
            url: homeUrl + "/api/package-charge/buy",
            type: "POST",
            dataType: 'json', headers: {
                'Authorization': sessionStorage.getItem("token"),
            },
            contentType: "application/json",
            data: JSON.stringify(dataCharge) ,
            success: function (response) {
                alert(response);
                window.location.href = homeUrl + "/buy-package";
            },
            error: function(jqXHR) {
                alert(jqXHR.responseText);
                window.location.href = homeUrl + "/buy-package";
            }
        });
    }

    $.ajax({
        url: homeUrl + "/api/gift-code/all",
        type: "GET",
        dataType: 'json',
        contentType: "application/json",
        success: function (response) {
            let html = "";
            $.each(response.data, function (index, value) {
                html += "<tr>\n" +
                    "            <td>" + (index + 1) + "</td>\n" +
                    "            <td>" + value.name + "</td>\n" +
                    "            <td>" + value.description + "</td>\n" +
                    "            <td>" + value.status + "</td>\n" +
                    "        </tr>";
            });
            $("#table-code").html(html);

        }
    });



    $.ajax({
        url: homeUrl + "/api/package-charge/my-history",
        type: "GET",
        dataType: 'json', headers: {
            'Authorization': sessionStorage.getItem("token"),
        },
        contentType: "application/json",
        success: function (response) {
            let html = "";
            $.each(response.data, function (index, value) {
                html += "        <tr>\n" +
                    "            <td>" + (index + 1) + "</td>\n" +
                    "            <td>" + value.playerName + "</td>\n" +
                    "            <td>" + value.serverName + "</td>\n" +
                    "            <td>" + value.packageChargeName + "</td>\n" +
                    "            <td>" + value.price + "</td>\n" +
                    "            <td>" + value.createdOn + "</td>\n" +
                    "        </tr>";
            });
            $("#list-charge").html(html);

        }
    });

    $(document).ready(function () {
        $.ajax({
            url: homeUrl + "/api/wallet/userCharge",
            type: "GET",
            dataType: 'json', headers: {
                'Authorization': sessionStorage.getItem("token"),
            },
            contentType: "application/json",
            success: function (response) {
                $("#total-charge").html(" Đã nạp vào hệ thống: " + response.data.totalCharge + " VNĐ");
                $("#amount").html(" Số dư khả dụng: " + response.data.amount + " VNĐ");
            }
        });
    });

    $.ajax({
        url: homeUrl + "/api/wallet/getAll",
        type: "GET",
        dataType: 'json', headers: {
            'Authorization': sessionStorage.getItem("token"),
        },
        contentType: "application/json",
        success: function (response) {
            let html = "";
            $.each(response.data, function (index, value) {
                html += "<tr>\n" +
                    "            <td>" + (index + 1) + "</td>\n" +
                    "            <td>" + value.createOn + "</td>\n" +
                    "            <td>" + value.typeNetwork + "</td>\n" +
                    "            <td>" + value.cardValue + "</td>\n" +
                    "            <td>" + value.status + "</td>\n" +
                    "        </tr>";
            });
            $("#my-history").html(html);

        }
    });

    $("#getPlayerId").click(function () {
        getPlayerId();
    });

    function getPlayerId() {
        var serverId        = $("#serverId").val();
        if(serverId == 0) {
            alert("Chọn server trước!");
            return;
        }
        $.ajax({
            url:  "/api/user/getPlayerId?serverId=" + serverId,
            type: "GET",
            dataType: 'json',
            headers: {
                'Authorization': sessionStorage.getItem("token"),
            },
            contentType: "application/json",
            success: function (response) {
                $("#playerName").val(response.data);
            }, error: function(jqXHR) {
                alert("Có lỗi xảy ra: "+ jqXHR.responseJSON.msg);
            }
        });
    }
});