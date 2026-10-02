param(
    [string] $Destination = (Join-Path $PSScriptRoot '../../../src/main/resources/assets/magnatour/textures/entity/pale_lord/pale_lord.png')
)

# Repack imagegen artwork into Minecraft's six-face cuboid UV convention.
# Each face uses nearest-neighbour sampling and a slightly reduced bark palette.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$source = [System.Drawing.Bitmap]::new((Join-Path $PSScriptRoot 'source.png'))
$atlas = [System.Drawing.Bitmap]::new(128, 128)
$graphics = [System.Drawing.Graphics]::FromImage($atlas)
$graphics.Clear([System.Drawing.Color]::FromArgb(0, 0, 0, 0))
$graphics.Dispose()

function Simplify-Bark([System.Drawing.Color] $color) {
    # Preserve amber eyes/resin and moss accents; consolidate close bark shades.
    if (($color.R -gt 170 -and $color.G -gt 65 -and $color.B -lt 110 -and $color.R - $color.B -gt 70) -or
        ($color.G - $color.B -gt 18 -and $color.R - $color.G -lt 22 -and $color.R -lt 175)) {
        return $color
    }
    $brightness = 0.299 * $color.R + 0.587 * $color.G + 0.114 * $color.B
    $level = [int]([Math]::Round($brightness / 24) * 24)
    $red = [Math]::Min(255, $level + 6)
    $green = [Math]::Min(255, $level + 2)
    $blue = [Math]::Max(0, [Math]::Min(255, $level - 4))
    return [System.Drawing.Color]::FromArgb(255, $red, $green, $blue)
}

function Copy-Face([int] $x, [int] $y, [int] $width, [int] $height, [int[]] $crop) {
    for ($row = 0; $row -lt $height; $row++) {
        for ($column = 0; $column -lt $width; $column++) {
            $sx = $crop[0] + [int][Math]::Floor(($column + 0.5) * $crop[2] / $width)
            $sy = $crop[1] + [int][Math]::Floor(($row + 0.5) * $crop[3] / $height)
            $atlas.SetPixel($x + $column, $y + $row, (Simplify-Bark $source.GetPixel($sx, $sy)))
        }
    }
}

function Copy-Net(
    [int] $u, [int] $v, [int] $width, [int] $height, [int] $depth,
    [int[]] $front, [int[]] $side, [int[]] $cap
) {
    Copy-Face ($u + $depth) $v $width $depth $cap
    Copy-Face ($u + $depth + $width) $v $width $depth $cap
    Copy-Face $u ($v + $depth) $depth $height $side
    Copy-Face ($u + $depth) ($v + $depth) $width $height $front
    Copy-Face ($u + $depth + $width) ($v + $depth) $depth $height $side
    Copy-Face ($u + 2 * $depth + $width) ($v + $depth) $width $height $side
}

try {
    if ($source.Width -ne 1254 -or $source.Height -ne 1254) {
        throw 'Expected the original 1254x1254 imagegen artwork.'
    }
    $bark = @(593, 99, 82, 135)
    $cap = @(594, 11, 80, 75)
    Copy-Net 0 0 6 10 6 @(80, 92, 99, 132) @(183, 94, 71, 130) @(82, 11, 98, 75)
    Copy-Net 0 24 10 14 6 @(179, 414, 134, 235) @(317, 415, 89, 233) @(181, 335, 130, 71)
    Copy-Net 40 0 4 10 4 $bark @(525, 99, 63, 134) $cap
    Copy-Net 60 0 4 9 4 @(849, 100, 78, 130) @(931, 100, 55, 130) @(849, 11, 78, 75)
    Copy-Net 40 28 4 18 4 @(595, 561, 77, 132) @(532, 561, 58, 132) @(595, 485, 78, 70)
    Copy-Net 60 28 4 18 4 @(850, 561, 76, 132) @(931, 562, 54, 130) @(850, 485, 77, 70)
    Copy-Net 84 0 2 7 2 @(1109, 112, 35, 99) $bark @(1086, 33, 31, 36)
    Copy-Net 84 12 2 10 2 @(1106, 368, 35, 151) $bark $cap
    Copy-Net 0 50 6 4 6 @(80, 846, 98, 92) @(183, 846, 77, 92) @(81, 770, 98, 67)
    Copy-Net 84 28 1 5 1 @(1113, 705, 18, 99) @(1113, 705, 18, 99) @(1110, 656, 33, 35)
    Copy-Net 40 54 5 3 8 @(520, 995, 115, 96) @(434, 995, 79, 96) @(515, 917, 112, 67)
    Copy-Net 72 54 5 3 8 @(919, 995, 131, 96) @(845, 995, 70, 96) @(919, 917, 126, 67)
    Copy-Net 0 64 4 6 1 @(80, 1058, 75, 94) @(23, 1058, 50, 94) @(77, 994, 76, 55)
    $atlas.Save([System.IO.Path]::GetFullPath($Destination), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $source.Dispose()
    $atlas.Dispose()
}
