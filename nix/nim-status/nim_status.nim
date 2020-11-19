import lib/shim as nim_shim


import go/shim as go_shim

let hashMessage {.exportc.} = go_shim.hashMessage
let generateAlias {.exportc.} = go_shim.generateAlias
let identicon {.exportc.} = go_shim.identicon
let initKeystore {.exportc.} = go_shim.initKeystore
let openAccounts {.exportc.} = go_shim.openAccounts
let multiAccountGenerateAndDeriveAddresses {.exportc.} = go_shim.multiAccountGenerateAndDeriveAddresses
let multiAccountStoreDerivedAccounts {.exportc.} = go_shim.multiAccountStoreDerivedAccounts
let multiAccountImportMnemonic {.exportc.} = go_shim.multiAccountImportMnemonic
let multiAccountImportPrivateKey {.exportc.} = go_shim.multiAccountImportPrivateKey
let multiAccountDeriveAddresses {.exportc.} = go_shim.multiAccountDeriveAddresses
let saveAccountAndLogin {.exportc.} = go_shim.saveAccountAndLogin
let deleteMultiAccount {.exportc.} = go_shim.deleteMultiAccount
let callRPC {.exportc.} = go_shim.callRPC
let callPrivateRPC {.exportc.} = go_shim.callPrivateRPC
let addPeer {.exportc.} = go_shim.addPeer
let sendTransaction {.exportc.} = go_shim.sendTransaction
let login {.exportc.} = go_shim.login
let logout {.exportc.} = go_shim.logout
let verifyAccountPassword {.exportc.} = go_shim.verifyAccountPassword
let validateMnemonic {.exportc.} = go_shim.validateMnemonic
let saveAccountAndLoginWithKeycard {.exportc.} = go_shim.saveAccountAndLoginWithKeycard
let hashTransaction {.exportc.} = go_shim.hashTransaction
let extractGroupMembershipSignatures {.exportc.} = go_shim.extractGroupMembershipSignatures
let connectionChange {.exportc.} = go_shim.connectionChange
let multiformatSerializePublicKey {.exportc.} = go_shim.multiformatSerializePublicKey
let multiformatDeserializePublicKey {.exportc.} = go_shim.multiformatDeserializePublicKey
let validateNodeConfig {.exportc.} = go_shim.validateNodeConfig
let loginWithKeycard {.exportc.} = go_shim.loginWithKeycard
let recover {.exportc.} = go_shim.recover
let writeHeapProfile {.exportc.} = go_shim.writeHeapProfile
let hashTypedData {.exportc.} = go_shim.hashTypedData
let resetChainData {.exportc.} = go_shim.resetChainData
let signMessage {.exportc.} = go_shim.signMessage
let signTypedData {.exportc.} = go_shim.signTypedData
let stopCPUProfiling {.exportc.} = go_shim.stopCPUProfiling
let getNodesFromContract {.exportc.} = go_shim.getNodesFromContract
let exportNodeLogs {.exportc.} = go_shim.exportNodeLogs
let chaosModeUpdate {.exportc.} = go_shim.chaosModeUpdate
let signHash {.exportc.} = go_shim.signHash
let sendTransactionWithSignature {.exportc.} = go_shim.sendTransactionWithSignature
let startCPUProfile {.exportc.} = go_shim.startCPUProfile
let appStateChange {.exportc.} = go_shim.appStateChange
let signGroupMembership {.exportc.} = go_shim.signGroupMembership
let multiAccountStoreAccount {.exportc.} = go_shim.multiAccountStoreAccount
let multiAccountLoadAccount {.exportc.} = go_shim.multiAccountLoadAccount
let multiAccountGenerate {.exportc.} = go_shim.multiAccountGenerate
let multiAccountReset {.exportc.} = go_shim.multiAccountReset
let migrateKeyStoreDir {.exportc.} = go_shim.migrateKeyStoreDir
let startWallet {.exportc.} = go_shim.startWallet
let stopWallet {.exportc.} = go_shim.stopWallet
let startLocalNotifications {.exportc.} = go_shim.startLocalNotifications
let stopLocalNotifications {.exportc.} = go_shim.stopLocalNotifications

type SignalCallback {.exportc: "SignalCallback".} = proc(eventMessage: cstring): void {.cdecl.}

proc setSignalEventCallback(callback: SignalCallback) {.exportc.} =
  go_shim.setSignalEventCallback(callback)

# proc setupForeignThreadGc {.exportc.} = 
#   echo ""
# proc tearDownForeignThreadGc {.exportc.} = 
#   echo ""
let setupForeignThreadGc {.exportc.} = (proc = discard)
let tearDownForeignThreadGc {.exportc.} = (proc = discard)

