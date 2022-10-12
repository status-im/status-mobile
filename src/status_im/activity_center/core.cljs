(ns status-im.activity-center.core
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.data-store.activities :as data-store.activities]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [status-im.utils.datetime :as utils.datetime]
            [taoensso.timbre :as log]))

;;;; Notification reconciliation

(def response-notifications
  [{:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
    :name "0x047c65",
    :accepted false,
    :type 5,
    :author
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
    :lastMessage nil,
    :id
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f20",
    :timestamp 1665167287000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAeElEQVR4nOzR0QnCMBhGURV3cR53cQx3cR6naRfoQ6EtXH7OeQwh5PI9bkMIqRFSI6RGSI2QGiE1QmqE1AipEVIzJuS59+L3/1u2zj+v9/2Mjxx9f8wiQmqE1AipEVIjpEZIjZAaIVxkzCJCaoTUCKkRUrMGAAD////ICGUlBQ5nAAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665167287000,
     :alias "Darkseagreen Colorless Honeybee",
     :seen true,
     :gapParameters {},
     :from
     "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
     :id
     "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x04892d0884fb44a940853ba988ce14b4c7c18351ea617bf29a5ceaa9eca03a8e1770f66282d9af9f5644578f8fcb8f4ff8fd926d8b389db4384ec91492aee8406d",
    :name "0x04892d",
    :accepted false,
    :type 5,
    :author
    "0x04892d0884fb44a940853ba988ce14b4c7c18351ea617bf29a5ceaa9eca03a8e1770f66282d9af9f5644578f8fcb8f4ff8fd926d8b389db4384ec91492aee8406d",
    :lastMessage nil,
    :id
    "0x04892d0884fb44a940853ba988ce14b4c7c18351ea617bf29a5ceaa9eca03a8e1770f66282d9af9f5644578f8fcb8f4ff8fd926d8b389db4384ec91492aee8406d20",
    :timestamp 1665100538000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAiElEQVR4nOzWUQmAYAwHcRW7GMwKxrCCwUyjBRSUMTj+3O/Rh0+Owdg0hDCExhAaQ2gMoYkJmbse3o/zevq+rcvY8b+YiRhCYwjN5w3SvYWq78dMxBAaQ2i8tWgMoTGExhAaQ2gMoSnfPW831V/VGyxmIobQGKImMRMxhMYQGkNoDKG5AwAA//9P5BRl2i6T5QAAAABJRU5ErkJggg==",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665100538000,
     :alias "Amused Neighboring Senegalpython",
     :seen true,
     :gapParameters {},
     :from
     "0x04892d0884fb44a940853ba988ce14b4c7c18351ea617bf29a5ceaa9eca03a8e1770f66282d9af9f5644578f8fcb8f4ff8fd926d8b389db4384ec91492aee8406d",
     :id
     "0x04892d0884fb44a940853ba988ce14b4c7c18351ea617bf29a5ceaa9eca03a8e1770f66282d9af9f5644578f8fcb8f4ff8fd926d8b389db4384ec91492aee8406d20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x04d7b7648d9ef3cacdb64755d2fed9a70e4eb1e287e5f59c58cb3eda2485bdc0d0fb0f88f87dcd568637f2213205defee3fbd864bfe279bf87e43301a32647b88f",
    :name "0x04d7b7",
    :accepted false,
    :type 5,
    :author
    "0x04d7b7648d9ef3cacdb64755d2fed9a70e4eb1e287e5f59c58cb3eda2485bdc0d0fb0f88f87dcd568637f2213205defee3fbd864bfe279bf87e43301a32647b88f",
    :lastMessage nil,
    :id
    "0x04d7b7648d9ef3cacdb64755d2fed9a70e4eb1e287e5f59c58cb3eda2485bdc0d0fb0f88f87dcd568637f2213205defee3fbd864bfe279bf87e43301a32647b88f20",
    :timestamp 1665097520000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAgUlEQVR4nOzWwQmAMBAFURV7sRI7sgw7shKr0XtASMCF4TPvHALDXv4yhTCExhAaQ2gMoYkJWXsfnvf1jHx8bPtc+U8r5iKG0BhCYwiNITSG0HRvrS+jm+qvDdaKuYghNIaoSMxFDKExhMYQGkNoDKExhMYQGkNoDKExhOYNAAD//0iXEGcISwhOAAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 3,
     :whisperTimestamp 1665097520000,
     :alias "Honeydew Golden Amazondolphin",
     :seen true,
     :gapParameters {},
     :from
     "0x04d7b7648d9ef3cacdb64755d2fed9a70e4eb1e287e5f59c58cb3eda2485bdc0d0fb0f88f87dcd568637f2213205defee3fbd864bfe279bf87e43301a32647b88f",
     :id
     "0x04d7b7648d9ef3cacdb64755d2fed9a70e4eb1e287e5f59c58cb3eda2485bdc0d0fb0f88f87dcd568637f2213205defee3fbd864bfe279bf87e43301a32647b88f20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x040185b964f42f2f11a07bb3275478605cf9f5a2f79af8a15d30baeaa9fe47e19d636d6461b797cfd2edf57a51e7be912c81e60698758bf145eaa611ae30f921df",
    :name "0x040185",
    :accepted false,
    :type 5,
    :author
    "0x040185b964f42f2f11a07bb3275478605cf9f5a2f79af8a15d30baeaa9fe47e19d636d6461b797cfd2edf57a51e7be912c81e60698758bf145eaa611ae30f921df",
    :lastMessage nil,
    :id
    "0x040185b964f42f2f11a07bb3275478605cf9f5a2f79af8a15d30baeaa9fe47e19d636d6461b797cfd2edf57a51e7be912c81e60698758bf145eaa611ae30f921df20",
    :timestamp 1665097287000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAfklEQVR4nOzYsQmAMABEURV3sXI7x3A7K6fRBSyEcPA5/istIp+DFFmmEobQGEJjCI0hNDUha+rg876er+/Hts+J/9UsYgiNITS/b5D0LTR6fs0ihtAYQmMIjSE0hiikZhFDaAyh8V2LxhAaQxRSs4ghNIbQGEJjCM0bAAD//5fvEGQwrcNWAAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665097287000,
     :alias "Quizzical Private Anteater",
     :seen true,
     :gapParameters {},
     :from
     "0x040185b964f42f2f11a07bb3275478605cf9f5a2f79af8a15d30baeaa9fe47e19d636d6461b797cfd2edf57a51e7be912c81e60698758bf145eaa611ae30f921df",
     :id
     "0x040185b964f42f2f11a07bb3275478605cf9f5a2f79af8a15d30baeaa9fe47e19d636d6461b797cfd2edf57a51e7be912c81e60698758bf145eaa611ae30f921df20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x046bf5a02fd3a28f82f656bbfbfcaf9bec021b3617c3dc043dc8353f4f6f009ac53df5eba2a2ae63b6f77413a1ad9a054f5c4509ce68b4bf0e688186ab95d2fca7",
    :name "0x046bf5",
    :accepted false,
    :type 5,
    :author
    "0x046bf5a02fd3a28f82f656bbfbfcaf9bec021b3617c3dc043dc8353f4f6f009ac53df5eba2a2ae63b6f77413a1ad9a054f5c4509ce68b4bf0e688186ab95d2fca7",
    :lastMessage nil,
    :id
    "0x046bf5a02fd3a28f82f656bbfbfcaf9bec021b3617c3dc043dc8353f4f6f009ac53df5eba2a2ae63b6f77413a1ad9a054f5c4509ce68b4bf0e688186ab95d2fca720",
    :timestamp 1665089696000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAgklEQVR4nOzWQQpAYABEYeQu9s7mGM5m7zTsZaE09Zret5R+vab+TEMJQ2gMoTGExhCampA5dfB+Htfb821Zx8T3ahYxhMYQms+3VvoW+nt+zSKG0BiikJpFDKExhCb2r5V+/6lmEUNoDFFIzSKG0BhCYwiNITSG0BhCYwjNHQAA///zHxhnUGfT1gAAAABJRU5ErkJggg==",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665089696000,
     :alias "Growling Marvelous Parrot",
     :seen true,
     :gapParameters {},
     :from
     "0x046bf5a02fd3a28f82f656bbfbfcaf9bec021b3617c3dc043dc8353f4f6f009ac53df5eba2a2ae63b6f77413a1ad9a054f5c4509ce68b4bf0e688186ab95d2fca7",
     :id
     "0x046bf5a02fd3a28f82f656bbfbfcaf9bec021b3617c3dc043dc8353f4f6f009ac53df5eba2a2ae63b6f77413a1ad9a054f5c4509ce68b4bf0e688186ab95d2fca720",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x044a2e346d30266b1b0a05def37b91a1578ab5e420c6971da1bde04d125c40b40a3ea1fe9519398b3fb354986096700f5887b67bb82a93f6e3dd151edc633b8f67",
    :name "0x044a2e",
    :accepted false,
    :type 5,
    :author
    "0x044a2e346d30266b1b0a05def37b91a1578ab5e420c6971da1bde04d125c40b40a3ea1fe9519398b3fb354986096700f5887b67bb82a93f6e3dd151edc633b8f67",
    :lastMessage nil,
    :id
    "0x044a2e346d30266b1b0a05def37b91a1578ab5e420c6971da1bde04d125c40b40a3ea1fe9519398b3fb354986096700f5887b67bb82a93f6e3dd151edc633b8f6720",
    :timestamp 1665075842000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAiUlEQVR4nOzWwQmEQBAF0V0xF2Pzbhjejc1oNIKRkR6h+NS7KxYNH6dfCENoDKExhMYQGkNoDKExhCYmZK6+4Dj3a8SHrMv2rzwfcxFDaAyhKS3Fk9aaVdepJeYihtAYQtO9IKP+qd7qXbmYixhCY4g+EnMRQ2gMoTGExhAaQ2gMoTGE5g4AAP//09cMY3Qm6ZsAAAAASUVORK5CYII=",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665075842000,
     :alias "Stable Sniveling Baldeagle",
     :seen true,
     :gapParameters {},
     :from
     "0x044a2e346d30266b1b0a05def37b91a1578ab5e420c6971da1bde04d125c40b40a3ea1fe9519398b3fb354986096700f5887b67bb82a93f6e3dd151edc633b8f67",
     :id
     "0x044a2e346d30266b1b0a05def37b91a1578ab5e420c6971da1bde04d125c40b40a3ea1fe9519398b3fb354986096700f5887b67bb82a93f6e3dd151edc633b8f6720",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x043e742bfa6dcd0c93689727ad02ac9e9d418f2a03a783b40d72bc8f0b3144707022ef84ae59a787977275c27ac9421ab094b8fb3908bb80f729f483eff38d84fc",
    :name "0x043e74",
    :accepted false,
    :type 5,
    :author
    "0x043e742bfa6dcd0c93689727ad02ac9e9d418f2a03a783b40d72bc8f0b3144707022ef84ae59a787977275c27ac9421ab094b8fb3908bb80f729f483eff38d84fc",
    :lastMessage nil,
    :id
    "0x043e742bfa6dcd0c93689727ad02ac9e9d418f2a03a783b40d72bc8f0b3144707022ef84ae59a787977275c27ac9421ab094b8fb3908bb80f729f483eff38d84fc20",
    :timestamp 1665075615000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAkklEQVR4nOzWwQnDMBAF0SSkl3SQylJGKksHqca+GwvWyGuGz7yzDho+CD1uIQyhMYTGEBpDaGJCntWD399/6b3Kvs/7da+ci1nEEBpD1CRmEUNoDKGZ/muN/kLd57diFjGExhCa8qt1VPW1OUvMIobQGEIz/bJc/acaiVnEEBpD1CRmEUNoDKExhMYQmjUAAP//o5AZOL4SLM8AAAAASUVORK5CYII=",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665075615000,
     :alias "Trusty Nautical Zethusspinipes",
     :seen true,
     :gapParameters {},
     :from
     "0x043e742bfa6dcd0c93689727ad02ac9e9d418f2a03a783b40d72bc8f0b3144707022ef84ae59a787977275c27ac9421ab094b8fb3908bb80f729f483eff38d84fc",
     :id
     "0x043e742bfa6dcd0c93689727ad02ac9e9d418f2a03a783b40d72bc8f0b3144707022ef84ae59a787977275c27ac9421ab094b8fb3908bb80f729f483eff38d84fc20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x0435b461849c0af696b2f71ad95e9c57053722a2390c833b304b4284b00466f52f940004d45f0fee149cf6ed65b8ff856921d40059344a01e68b2fa9dae82e4900",
    :name "0x0435b4",
    :accepted false,
    :type 5,
    :author
    "0x0435b461849c0af696b2f71ad95e9c57053722a2390c833b304b4284b00466f52f940004d45f0fee149cf6ed65b8ff856921d40059344a01e68b2fa9dae82e4900",
    :lastMessage nil,
    :id
    "0x0435b461849c0af696b2f71ad95e9c57053722a2390c833b304b4284b00466f52f940004d45f0fee149cf6ed65b8ff856921d40059344a01e68b2fa9dae82e490020",
    :timestamp 1665074686000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAgUlEQVR4nOzWsQmAMBgFYRV3cTwbx7BxPKfRPigYJOR43FdKEI7X/NMQwhAaQ2gMoTGEJiZk/vrw2M/r6fu6LWOP96WYRQyhMUSNxCxiCI0hNL9vrTe1N1Xtf0oxixhCYwiNITSG0BiiRmIWMYTGEBpDaAyhMYTGEBpDaO4AAAD//5TbFGUr+nc/AAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 3,
     :whisperTimestamp 1665074686000,
     :alias "Remote Vapid Honeyeater",
     :seen true,
     :gapParameters {},
     :from
     "0x0435b461849c0af696b2f71ad95e9c57053722a2390c833b304b4284b00466f52f940004d45f0fee149cf6ed65b8ff856921d40059344a01e68b2fa9dae82e4900",
     :id
     "0x0435b461849c0af696b2f71ad95e9c57053722a2390c833b304b4284b00466f52f940004d45f0fee149cf6ed65b8ff856921d40059344a01e68b2fa9dae82e490020",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x04f18135a0d5e904d5a877c24ad286d227b1a91ee583d61f16ba71de801d9d92df330696c5788b0810999c4710271584ec84eac223bc5f7f9eaf483b0ed2bd2093",
    :name "0x04f181",
    :accepted false,
    :type 5,
    :author
    "0x04f18135a0d5e904d5a877c24ad286d227b1a91ee583d61f16ba71de801d9d92df330696c5788b0810999c4710271584ec84eac223bc5f7f9eaf483b0ed2bd2093",
    :lastMessage nil,
    :id
    "0x04f18135a0d5e904d5a877c24ad286d227b1a91ee583d61f16ba71de801d9d92df330696c5788b0810999c4710271584ec84eac223bc5f7f9eaf483b0ed2bd209320",
    :timestamp 1665073831000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAkElEQVR4nOzY0QmAMAwGYRV3cQI3cww3cwKn0QUUAm3h+LnvUYpwBELpMoUwhMYQGkNoDKGJCVlbf3Cd9/P1fT+2ucf5qpiJGEJjCE15U/xtm9Gq2yxmIobQGELTfNfqdadq3YoxEzGExhAaQ2gMoTGEZti7Vq/zVTETMYTGEA0SMxFDaAyhMYTGEJo3AAD//6aKGsYhz2MYAAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665073831000,
     :alias "Nippy Idolized Armyworm",
     :seen true,
     :gapParameters {},
     :from
     "0x04f18135a0d5e904d5a877c24ad286d227b1a91ee583d61f16ba71de801d9d92df330696c5788b0810999c4710271584ec84eac223bc5f7f9eaf483b0ed2bd2093",
     :id
     "0x04f18135a0d5e904d5a877c24ad286d227b1a91ee583d61f16ba71de801d9d92df330696c5788b0810999c4710271584ec84eac223bc5f7f9eaf483b0ed2bd209320",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}
   {:replyMessage nil,
    :read true,
    :dismissed false,
    :chatId
    "0x04b5aed12d7de0f72a7e3494b7d3ab20b316a9fde5732a3eb1d0f41a957900f764d725a3e725152c37bb615ab2251349ef84e92a145333d8ece9a598cae75681e7",
    :name "0x04b5ae",
    :accepted false,
    :type 5,
    :author
    "0x04b5aed12d7de0f72a7e3494b7d3ab20b316a9fde5732a3eb1d0f41a957900f764d725a3e725152c37bb615ab2251349ef84e92a145333d8ece9a598cae75681e7",
    :lastMessage nil,
    :id
    "0x04b5aed12d7de0f72a7e3494b7d3ab20b316a9fde5732a3eb1d0f41a957900f764d725a3e725152c37bb615ab2251349ef84e92a145333d8ece9a598cae75681e720",
    :timestamp 1665064413000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAm0lEQVR4nOzXsQnDMBBA0SRkl7QZLmNkOLeexm5cCcmcsIQ/x3+lUfM5OM6vRxKG0BhCYwiNITRpQt7Rh/912Wrff5/v8473pTQTMYTGEJrQRjjT2ja9otupJc1EDKExhGbardXLW+tgCI0hNOGt1TLqT/CqNBMxhMYQmiF3Us3s26yUZiKG0BiiSdJMxBAaQ2gMoTGEZg8AAP//9ecjm0YKWeEAAAAASUVORK5CYII=",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665064413000,
     :alias "Scary Quickwitted Wrasse",
     :seen true,
     :gapParameters {},
     :from
     "0x04b5aed12d7de0f72a7e3494b7d3ab20b316a9fde5732a3eb1d0f41a957900f764d725a3e725152c37bb615ab2251349ef84e92a145333d8ece9a598cae75681e7",
     :id
     "0x04b5aed12d7de0f72a7e3494b7d3ab20b316a9fde5732a3eb1d0f41a957900f764d725a3e725152c37bb615ab2251349ef84e92a145333d8ece9a598cae75681e720",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}])

(def response-unread-notifications
  [{:replyMessage nil,
    :read false,
    :dismissed false,
    :chatId
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
    :name "0x047c65",
    :accepted false,
    :type 5,
    :author
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
    :lastMessage nil,
    :id
    "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f20",
    :timestamp 1665167287000,
    :message
    {:messageType 0,
     :identicon
     "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADIAAAAyCAYAAAAeP4ixAAAAeElEQVR4nOzR0QnCMBhGURV3cR53cQx3cR6naRfoQ6EtXH7OeQwh5PI9bkMIqRFSI6RGSI2QGiE1QmqE1AipEVIzJuS59+L3/1u2zj+v9/2Mjxx9f8wiQmqE1AipEVIjpEZIjZAaIVxkzCJCaoTUCKkRUrMGAAD////ICGUlBQ5nAAAAAElFTkSuQmCC",
     :replace "",
     :chatId "",
     :displayName "",
     :lineCount 0,
     :contactRequestState 2,
     :whisperTimestamp 1665167287000,
     :alias "Darkseagreen Colorless Honeybee",
     :seen true,
     :gapParameters {},
     :from
     "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f",
     :id
     "0x047c654a4b35e4c3605c0eacf68364dd43140616e4d96ae362c4bf7c5ae3f1a3c03ad5935ae1face369bf96edda4ec02459b33964f1a7781036824f1e528c21b2f20",
     :parsedText
     [{:type "paragraph",
       :children [{:literal "Please add me to your contacts"}]}],
     :contentType 11,
     :clock 0,
     :localChatId "",
     :timestamp 0,
     :ensName "",
     :quotedMessage nil,
     :rtl false,
     :responseTo "",
     :text "Please add me to your contacts"}}])

(defn- update-notifications
  "Insert `new-notifications` in `db-notifications`.

  Although correct, this is a naive implementation for reconciling notifications
  because for every notification in `new-notifications`, linear scans will be
  performed to remove it and sorting will be performed for every new insertion.
  If the number of existing notifications cached in the app db becomes
  ~excessively~ big, this implementation will probably need to be revisited."
  [db-notifications new-notifications]
  (reduce (fn [acc {:keys [id type read] :as notification}]
            (let [filter-status (if read :read :unread)]
              (cond-> (-> acc
                          (update-in [type :read :data]
                                     (fn [data]
                                       (remove #(= id (:id %)) data)))
                          (update-in [type :unread :data]
                                     (fn [data]
                                       (remove #(= id (:id %)) data))))
                (not (or (:dismissed notification) (:accepted notification)))
                (update-in [type filter-status :data]
                           (fn [data]
                             (->> notification
                                  (conj data)
                                  (sort-by (juxt :timestamp :id))
                                  reverse))))))
          db-notifications
          new-notifications))

(fx/defn notifications-reconcile
  {:events [:activity-center.notifications/reconcile]}
  [{:keys [db]} new-notifications]
  (when (seq new-notifications)
    {:db (update-in db [:activity-center :notifications]
                    update-notifications new-notifications)}))

;;;; Notifications fetching and pagination

(def defaults
  {:filter-status          :unread
   :filter-type            constants/activity-center-notification-type-no-type
   :notifications-per-page 10})

(def start-or-end-cursor
  "")

(defn- valid-cursor?
  [cursor]
  (and (some? cursor)
       (not= cursor start-or-end-cursor)))

(defn- filter-status->rpc-method
  [filter-status]
  (if (= filter-status :read)
    "wakuext_readActivityCenterNotifications"
    "wakuext_unreadActivityCenterNotifications"))

(fx/defn notifications-fetch
  [{:keys [db]} {:keys [cursor filter-type filter-status reset-data?]}]
  (when-not (get-in db [:activity-center :notifications filter-type filter-status :loading?])
    {:db             (assoc-in db [:activity-center :notifications filter-type filter-status :loading?] true)
     ::json-rpc/call [{:method     (filter-status->rpc-method filter-status)
                       :params     [cursor (defaults :notifications-per-page) filter-type]
                       :on-success #(re-frame/dispatch [:activity-center.notifications/fetch-success filter-type filter-status reset-data? %])
                       :on-error   #(re-frame/dispatch [:activity-center.notifications/fetch-error filter-type filter-status %])}]}))

(fx/defn notifications-fetch-first-page
  {:events [:activity-center.notifications/fetch-first-page]}
  [{:keys [db] :as cofx} {:keys [filter-type filter-status]}]
  (tap> {:EVT-fetch-first-page (utils.datetime/now->iso8601)})
  (let [filter-type   (or filter-type
                          (get-in db [:activity-center :filter :type]
                                  (defaults :filter-type)))
        filter-status (or filter-status
                          (get-in db [:activity-center :filter :status]
                                  (defaults :filter-status)))]
    (as-> {:db (-> db
                   (assoc-in [:activity-center :filter :type] filter-type)
                   (assoc-in [:activity-center :filter :status] filter-status)
                   (assoc-in [:activity-center :notifications filter-type filter-status :loading?] true)
                   ;; Comment this assoc call and the UI becomes less responsive.
                   (assoc-in [:activity-center :notifications filter-type filter-status :data] [])
                   )}
        cofx'
      (if (and (= 0 filter-type) (= :read filter-status))
        (assoc cofx' :dispatch-later [;; Lower the delay and eventually the UI becomes less responsive.
                                      {:ms       50
                                       :dispatch [:activity-center.notifications/fetch-success
                                                  filter-type
                                                  filter-status
                                                  true
                                                  {:cursor "" :notifications response-notifications}]}])
        (if (and (= 0 filter-type) (= :unread filter-status))
          (assoc cofx' :dispatch-later [;; Lower the delay and eventually the UI becomes less responsive.
                                        {:ms       50
                                         :dispatch [:activity-center.notifications/fetch-success
                                                    filter-type
                                                    filter-status
                                                    true
                                                    {:cursor "" :notifications response-unread-notifications}]}])
          (assoc cofx' :dispatch [:activity-center.notifications/fetch-success
                                  filter-type
                                  filter-status
                                  true
                                  {:cursor "" :notifications []}]))))))

(fx/defn notifications-fetch-next-page
  {:events [:activity-center.notifications/fetch-next-page]}
  [{:keys [db] :as cofx}]
  (let [{:keys [type status]} (get-in db [:activity-center :filter])
        {:keys [cursor]}      (get-in db [:activity-center :notifications type status])]
    (when (valid-cursor? cursor)
      (tap> {:EVT-fetch-next-page (utils.datetime/now->iso8601)})
      (notifications-fetch cofx {:cursor        cursor
                                 :filter-type   type
                                 :filter-status status
                                 :reset-data?   false}))))

(fx/defn notifications-fetch-success
  {:events [:activity-center.notifications/fetch-success]}
  [{:keys [db]}
   filter-type
   filter-status
   reset-data?
   {:keys [cursor notifications]}]
  (tap> {:EVT-fetch-success (utils.datetime/now->iso8601)})
  (let [processed (map data-store.activities/<-rpc notifications)]
    {:db (-> db
             (assoc-in [:activity-center :notifications filter-type filter-status :cursor] cursor)
             (update-in [:activity-center :notifications filter-type filter-status] dissoc :loading?)
             (update-in [:activity-center :notifications filter-type filter-status :data]
                        (if reset-data?
                          (constantly processed)
                          #(concat %1 processed))))}))

(fx/defn notifications-fetch-error
  {:events [:activity-center.notifications/fetch-error]}
  [{:keys [db]} filter-type filter-status error]
  (log/warn "Failed to load Activity Center notifications" error)
  {:db (update-in db [:activity-center :notifications filter-type filter-status] dissoc :loading?)})

(fx/defn notifications-quick-toggle
  {:events [:activity-center.notifications/quick-toggle]}
  [{:keys [db]}]
  {:db (assoc db :activity-center-notifications-filter-status
              (if (= :read (:activity-center-notifications-filter-status db))
                :unread
                :read))})

(comment
  (fx/defn notifications-quick-toggle
    {:events [:activity-center.notifications/quick-toggle]}
    [{:keys [db]}]
    {:db (assoc-in db [:activity-center :filter :status]
                   (if (= :read (get-in db [:activity-center :filter :status]))
                     :unread
                     :read))})

  (re-frame/reg-event-db
   :dev/reset-activity-center
   (fn [db]
     (-> db
         (assoc-in [:activity-center]
                   {:filter        {:status :read
                                    :type   0}
                    :notifications {}}))))

  (re-frame/dispatch [:dev/reset-activity-center]))
