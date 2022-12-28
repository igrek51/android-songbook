package igrek.songbook.settings.sync

import org.junit.Test
import kotlin.test.assertEquals

class BackupEncoderTest {

    @Test
    fun test_aes_encryption() {
        val inputText = "abcdefghiDUPAgklmnopqrstuvwxyz0123456789".toByteArray()
        val encoder = BackupEncoder()
        val encrypted: ByteArray = encoder.aesEncrypt(inputText)
        println(encrypted)
        encoder.aesEncrypt(inputText)
        val decrypted: ByteArray = encoder.aesDecrypt(encrypted)
        assertEquals(String(decrypted), String(inputText))
        // idempotence
        assertEquals(String(encoder.aesEncrypt(inputText)), String(encrypted))
    }

    @Test
    fun test_decrypt() {
        val encodedData = "IJVsRLuIrLdk9MPn+8qCEfOIfctbXdWhqxKv7n++MvirFsJqQ7wJMN7EgS6aUGQa5ALl9IOAqaivsY1U1lZqlqbHk3yFgXzA1zjugibs/sPn6OMg0M3ABp3lOF2j+iN5g6YEF5CICG1dnRG2Y/uhjmZtLj69jjmdKHsT/e4Kp6DFjYep/sVflg4qmfffd0AoKTuzh9FhiqUmELqnM9A/Ik76lHAJNi4e57LIZO8iNQ3pjlggY+e1MsIRcmkqKiqFiBn3B6zBzrG3UpUl+6J0D9UPEPbgaIL7moPxc0pSJ0r7critJ165qOuwwLn+ucImnKCXFr6w92Wg1FsdugU936qCEqGdfuFNvJK3HZEtExmsj2Fpw2tDPR1lBg5aFO8seFuI8P9nhGm7VDFTq4zmBSCQx45dEpSaagBE6WxQ3HJdybeAwU0gRyxr6WijGHMPRE+wtjtShaYR25H72h60L7Iy69yvQCtJ9pHDxN2AHsvTDUqQy5yk51DjHBT+VTvNkw+GbDeevSy/L/Ud0lU0ARjxG/VLufyvtM+chOSh7CTIaWuKS4lROb5FmU0usaCQkbURC1azKJfffqf/cQ1UVJYrhryaJzRkff6l3zhOMe4hW1pMPwp/jJqRyZyWX9suJ+0iRCr+X2NT1TQIdFPrrRtuqGNgpHdZP1eoSL4mVBMa6bYRo1p2NbCYdYILFcd4qhZHWgmVOicBCx1YfBVWRbgr49wDwGdOpyq5OV2O36HQ1yZgipvMxSmfHXNVDf6pYZVLkP81dVxU+uzHjvXnGxXy5yxNAH77gFUpFHfLTUlIcBsLq3KId4DSIvU+cmWgaSnCSKeEayNf28UovmMtmKxOhcSH1pcZY/eqQw37bIJwtPJKIA4lRibHP3JjqBwz+NeSbMJD/Iqp5HB1FgA+NMIVseHNL4JqHSUd34bKlUyDBrCN+K1TnPRdd3DAFzGwp1hSjoOknyBUJEnZvw/n9Q=="
        val decoded = String(BackupEncoder().decodeFileBackup(encodedData))
        println(decoded)
        assertEquals(decoded, "{\"songs\":[{\"id\":3,\"title\":\"pink panther\",\"categoryName\":null,\"content\":\"\",\"versionNumber\":1,\"createTime\":1586458595619,\"updateTime\":1586458595619,\"comment\":null,\"preferredKey\":null,\"metre\":null,\"author\":null,\"language\":null,\"scrollSpeed\":null,\"initialDelay\":null,\"chordsNotation\":\"GERMAN\",\"originalSongId\":null},{\"id\":9,\"title\":\"Spanish Romance\",\"categoryName\":null,\"content\":\"[e]\",\"versionNumber\":1,\"createTime\":1586458583852,\"updateTime\":1633948575715,\"comment\":null,\"preferredKey\":null,\"metre\":null,\"author\":null,\"language\":null,\"scrollSpeed\":null,\"initialDelay\":null,\"chordsNotation\":\"GERMAN\",\"originalSongId\":null},{\"id\":16,\"title\":\"Snow\",\"categoryName\":\"Heroes3\",\"content\":\"\",\"versionNumber\":1,\"createTime\":1600931113912,\"updateTime\":1600931113912,\"comment\":null,\"preferredKey\":null,\"metre\":null,\"author\":null,\"language\":null,\"scrollSpeed\":null,\"initialDelay\":null,\"chordsNotation\":\"GERMAN\",\"originalSongId\":null},{\"id\":26,\"title\":\"Comments\",\"categoryName\":null,\"content\":\"{this is title}\\nsomeZthing\\ncomemnt [a] in {in line} awesome\",\"versionNumber\":1,\"createTime\":1622544778937,\"updateTime\":1627753328391,\"comment\":null,\"preferredKey\":null,\"metre\":null,\"author\":null,\"language\":null,\"scrollSpeed\":null,\"initialDelay\":null,\"chordsNotation\":\"GERMAN\",\"originalSongId\":null},{\"id\":27,\"title\":\"Bunker\",\"categoryName\":\"Balthazar\",\"content\":\"[Fm Ab Bbm Cm]\\n\\nEvery time I [Fm]walk on by\\nStroll along your [Ab]street\\nCan't believe there's not a [Bbm]thing in the world\\nA thing left to [Cm]repeat\\n\\nAnd I was walking [Fm]on your floor\\nBegging to get [Ab]more\\nCan't believe there's not a [Bbm]thing in the world\\nA thing left to ask [Cm]for\\n\\nSo leave my broken [Fm]bones\\nI'll take a load of [Ab]your skin\\nThrow me all your [Bbm]stones\\nYou need a sinner, [Cm]I'm in, yeah\\n\\nEvery time I [Fm]walk on by\\nStroll along your [Ab]street\\nCan't believe there's not a [Bbm]thing in the world\\nA thing left to [Cm]repeat\\n\\nSo leave my broken [Fm]bones\\nI'll take a load of [Ab]your skin\\nThrow me all your [Bbm]stones\\nYou need a sinner, [Cm]I'm in, yeah\\n\\nSo leave my broken [Fm]bones\\nI'll take a load of [Ab]your skin\\nThrow me all your [Bbm]stones\\nYou need a sinner, [Cm]I'm in, yeah\\n\\n[Fm]Don't you know\\nDon't you know what to [Ab]say\\nEvery time you [Bbm]think about it\\nTalk around it [Cm]again\\n\\n[Fm]Don't you know\\nDon't you know where to [Ab]stay\\nEvery time you [Bbm]think about it\\nTalk around it [Cm]again\\n\\n[Fm]Don't you know\\nDon't you know what to [Ab]say\\nEvery time you [Bbm]think about it\\nTalk around it [Cm]again\\n\\n[Fm]Don't you know\\nDon't you know where to [Ab]stay\\nEvery time you [Bbm]think about it\\nTalk around it [Cm]again\\n\\n[Fm Ab Bbm Cm]\\n[Fm Ab Bbm Cm]\",\"versionNumber\":1,\"createTime\":1672242023490,\"updateTime\":1672242023490,\"comment\":null,\"preferredKey\":null,\"metre\":null,\"author\":null,\"language\":null,\"scrollSpeed\":null,\"initialDelay\":null,\"chordsNotation\":\"ENGLISH\",\"originalSongId\":null}]}")
    }

}
