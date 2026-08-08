$User = "sscrip"
$Version = "v1.0.0"

$Services = @{
    "api-gateway"            = "bu-gateway"
    "eureka-server-cursos"   = "bu-eureka"
    "ms-user-auth-register"  = "bu-auth"
    "ms-prestamos"           = "bu-prestamos"
    "ms-catalogo"            = "bu-catalogo"
    "ms-devoluciones"        = "bu-devoluciones"
    "BiblioUTEQFront"        = "bu-frontend"
}

foreach ($dir in $Services.Keys) {
    $name = $Services[$dir]
    Write-Host "== Building $name ==" -ForegroundColor Cyan

    docker build -t "$User/${name}:$Version" ".\$dir"
    if (-not $?) { throw "Build failed for $name" }

    docker tag "$User/${name}:$Version" "$User/${name}:latest"

    docker push "$User/${name}:$Version"
    if (-not $?) { throw "Push failed for $name ($Version)" }

    docker push "$User/${name}:latest"
    if (-not $?) { throw "Push failed for $name (latest)" }
}

Write-Host "Listo, todas las imagenes construidas y subidas." -ForegroundColor Green
