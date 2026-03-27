# PowerShell script for testing LastMile API
# Run from lastmile-system directory

$baseUrl = "http://localhost:8080/api/v1"

# Helper function
function Invoke-Api {
    param(
        [string]$Method,
        [string]$Endpoint,
        [string]$Token = "",
        [string]$Body = ""
    )
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token) { $headers["Authorization"] = "Bearer $Token" }
    
    $params = @{
        Uri = "$baseUrl$Endpoint"
        Method = $Method
        Headers = $headers
    }
    if ($Body) { $params["Body"] = $Body }
    
    try {
        $response = Invoke-RestMethod @params
        return $response | ConvertTo-Json -Depth 10
    } catch {
        return "ERROR: $($_.Exception.Message)"
    }
}

Write-Host "=== LASTMILE API TEST SCRIPT ===" -ForegroundColor Cyan

# 1. Get tokens
Write-Host "`n--- Getting Auth Tokens ---" -ForegroundColor Yellow
$adminLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"password"}'
$ADMIN_TOKEN = $adminLogin.data.token
Write-Host "Admin token obtained: $($ADMIN_TOKEN.Substring(0,20))..."

$dispatcherLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"dispatcher","password":"password"}'
$DISPATCHER_TOKEN = $dispatcherLogin.data.token
Write-Host "Dispatcher token obtained"

$carlosLogin = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -ContentType "application/json" -Body '{"username":"carlos","password":"password"}'
$CARLOS_TOKEN = $carlosLogin.data.token
$CARLOS_COURIER_ID = $carlosLogin.data.courierId
Write-Host "Carlos token obtained, courierId: $CARLOS_COURIER_ID"

# 2. Get couriers
Write-Host "`n--- Getting Couriers ---" -ForegroundColor Yellow
$couriers = Invoke-RestMethod -Uri "$baseUrl/couriers" -Headers @{ Authorization = "Bearer $ADMIN_TOKEN" }
Write-Host "Couriers: $($couriers.data | ConvertTo-Json -Compress)"

# 3. Create test orders
Write-Host "`n--- SECTION 2: Creating Orders ---" -ForegroundColor Green
$orderBody1 = @{
    senderName = "Almacen Central"
    senderPhone = "+51999000001"
    senderAddress = "Av. Industrial 123"
    senderAddressReference = "Junto al parque"
    senderLatitude = -12.0464
    senderLongitude = -77.0428
    recipientName = "Juan Perez"
    recipientPhone = "+51999111001"
    recipientEmail = "juan@test.com"
    recipientAddress = "Calle Los Olivos 456"
    recipientAddressReference = "Casa azul"
    recipientLatitude = -12.0500
    recipientLongitude = -77.0500
    weightKg = 2.5
    volumeCm3 = 5000
    deliveryNotes = "Fragil"
} | ConvertTo-Json

$order1 = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers @{ Authorization = "Bearer $ADMIN_TOKEN"; "Content-Type" = "application/json" } -Body $orderBody1
Write-Host "Order 1 created: $($order1.data.id) - Status: $($order1.data.status)"
$ORDER1_ID = $order1.data.id
$ORDER1_TRACKING = $order1.data.trackingCode

$orderBody2 = @{
    senderName = "Almacen Central"
    senderPhone = "+51999000001"
    senderAddress = "Av. Industrial 123"
    senderAddressReference = "Junto al parque"
    senderLatitude = -12.0464
    senderLongitude = -77.0428
    recipientName = "Maria Garcia"
    recipientPhone = "+51999111002"
    recipientEmail = "maria@test.com"
    recipientAddress = "Av. Brasil 789"
    recipientAddressReference = "Edificio rojo"
    recipientLatitude = -12.0550
    recipientLongitude = -77.0450
    weightKg = 1.0
    volumeCm3 = 2000
    deliveryNotes = "Llamar antes"
} | ConvertTo-Json

$order2 = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers @{ Authorization = "Bearer $ADMIN_TOKEN"; "Content-Type" = "application/json" } -Body $orderBody2
Write-Host "Order 2 created: $($order2.data.id) - Status: $($order2.data.status)"
$ORDER2_ID = $order2.data.id
$ORDER2_TRACKING = $order2.data.trackingCode

$orderBody3 = @{
    senderName = "Almacen Central"
    senderPhone = "+51999000001"
    senderAddress = "Av. Industrial 123"
    senderAddressReference = "Junto al parque"
    senderLatitude = -12.0464
    senderLongitude = -77.0428
    recipientName = "Pedro Lopez"
    recipientPhone = "+51999111003"
    recipientEmail = "pedro@test.com"
    recipientAddress = "Jr. Union 321"
    recipientAddressReference = "Cerca al mercado"
    recipientLatitude = -12.0600
    recipientLongitude = -77.0400
    weightKg = 3.0
    volumeCm3 = 8000
    deliveryNotes = "Dejar con portero"
} | ConvertTo-Json

$order3 = Invoke-RestMethod -Uri "$baseUrl/orders" -Method Post -Headers @{ Authorization = "Bearer $ADMIN_TOKEN"; "Content-Type" = "application/json" } -Body $orderBody3
Write-Host "Order 3 created: $($order3.data.id) - Status: $($order3.data.status)"
$ORDER3_ID = $order3.data.id
$ORDER3_TRACKING = $order3.data.trackingCode

# 4. Mark orders as READY_TO_DISPATCH
Write-Host "`n--- Marking Orders as READY_TO_DISPATCH ---" -ForegroundColor Yellow
Invoke-RestMethod -Uri "$baseUrl/orders/$ORDER1_ID/ready" -Method Post -Headers @{ Authorization = "Bearer $DISPATCHER_TOKEN" }
Write-Host "Order 1 marked ready"
Invoke-RestMethod -Uri "$baseUrl/orders/$ORDER2_ID/ready" -Method Post -Headers @{ Authorization = "Bearer $DISPATCHER_TOKEN" }
Write-Host "Order 2 marked ready"
Invoke-RestMethod -Uri "$baseUrl/orders/$ORDER3_ID/ready" -Method Post -Headers @{ Authorization = "Bearer $DISPATCHER_TOKEN" }
Write-Host "Order 3 marked ready"

# 5. Create route and assign orders
Write-Host "`n--- Creating Route and Assigning Orders ---" -ForegroundColor Yellow
$routeBody = @{
    courierId = $CARLOS_COURIER_ID
    date = (Get-Date).ToString("yyyy-MM-dd")
    orderIds = @($ORDER1_ID, $ORDER2_ID, $ORDER3_ID)
} | ConvertTo-Json

$route = Invoke-RestMethod -Uri "$baseUrl/dispatch/routes" -Method Post -Headers @{ Authorization = "Bearer $DISPATCHER_TOKEN"; "Content-Type" = "application/json" } -Body $routeBody
Write-Host "Route created: $($route.data.id) - Status: $($route.data.status)"
$ROUTE_ID = $route.data.id

# 6. Verify orders are now ASSIGNED
Write-Host "`n--- Verifying Order Statuses (should be ASSIGNED) ---" -ForegroundColor Yellow
$ordersCheck = Invoke-RestMethod -Uri "$baseUrl/orders?status=ASSIGNED" -Headers @{ Authorization = "Bearer $ADMIN_TOKEN" }
Write-Host "ASSIGNED orders count: $($ordersCheck.data.Count)"

# Output variables for next sections
Write-Host "`n=== TEST DATA ===" -ForegroundColor Cyan
Write-Host "ORDER1_ID=$ORDER1_ID"
Write-Host "ORDER1_TRACKING=$ORDER1_TRACKING"
Write-Host "ORDER2_ID=$ORDER2_ID"
Write-Host "ORDER2_TRACKING=$ORDER2_TRACKING"
Write-Host "ORDER3_ID=$ORDER3_ID"
Write-Host "ORDER3_TRACKING=$ORDER3_TRACKING"
Write-Host "ROUTE_ID=$ROUTE_ID"
Write-Host "CARLOS_COURIER_ID=$CARLOS_COURIER_ID"
Write-Host "CARLOS_TOKEN=$($CARLOS_TOKEN.Substring(0,50))..."
