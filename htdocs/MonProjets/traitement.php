<?php
session_start();
$email = $_POST['email'];
$password = $_POST['password'];
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="resultat.css">
    <title>Résultat Connexion</title>
</head>

<body>
    <div class="container">
        <?php
        if ($_POST['email'] == "antoniabijou@gmail.com" && $_POST['password'] == "1234") {
            echo "<h1>Bienvenue, " . htmlspecialchars($_POST['email']) . "!</h1>";
        } else {
            echo "<h1>Connexion échouée</h1>";
            echo "<p>L'email ou le mot de passe est incorrect. Veuillez vérifier vos informations et réessayer.</p>";
        }
        ?>
        <a href="index.php" class="btn">Retour</a>
    </div>
</body>

</html>