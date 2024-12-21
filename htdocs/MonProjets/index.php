<!DOCTYPE html>
<html lang="fr">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Formulaire de Connexion</title>
    <link rel="stylesheet" href="login.css">

</head>

<body>
    <div class="container">
        <form action="traitement.php" method="post">
            <div class="title">
                <span>Login</span>
            </div>
            <div class="element">
                <label for="email">Email:</label>
                <input type="email" id="email" name="email" placeholder="Email" required>
                <label for="password">Mot de passe:</label>
                <input type="password" id="password" name="password" placeholder="Mot de passe" required>

                <div class="ePassword">
                    <label for="showPassword">Show Password</label>
                    <a href="#">Forgot Password ?</a>
                </div>
                <button type="submit" id="btn_login">LOGIN</button>
                <div class="exeption">
                    <span>
                        Don't have an account? <a href="#">Sign up</a>
                    </span>
                </div>

        </form>
        <!-- <form action="traitement.php" method="GET">
            <input type="number" name="number" placeholder="Saisir un nombre">
            <input type="submit" value="Se connecter">
        </form> -->


    </div>
    </div>
</body>

</html>