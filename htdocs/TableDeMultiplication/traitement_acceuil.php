<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="styles.css">
    <title>Résultat Table de Multiplication</title>
</head>
<body>
    <div class="container">
        <h1>Table de Multiplication de <?php echo htmlspecialchars($_GET['number']); ?></h1>
        <ul>
            <?php
                $number = (int)$_GET['number'];
                for ($i = 1; $i <= 10; $i++) {
                    echo "<li>" . htmlspecialchars($number) . " × " . $i . " = " . ($number * $i) . "</li>";
                }
            ?>
        </ul>
        <a href="index.html" class="btn">Retour</a>
    </div>
</body>
</html>
