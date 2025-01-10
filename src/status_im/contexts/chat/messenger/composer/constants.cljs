(ns status-im.contexts.chat.messenger.composer.constants)

(def ^:const actions-container-height 56)

(def ^:const images-padding-top 12)
(def ^:const images-padding-bottom 8)
(def ^:const images-container-height
  (+ actions-container-height images-padding-top images-padding-bottom))

(def ^:const links-padding-top 12)
(def ^:const links-padding-bottom 8)
(def ^:const links-container-height
  (+ actions-container-height links-padding-top links-padding-bottom))

(def ^:const reply-container-height 32)

(def ^:const edit-container-height 32)

(def ^:const mentions-max-height 240)

(def ^:const max-text-size 4096)

(def ^:const unfurl-debounce-ms
  "Use a high threshold to prevent unnecessary rendering overhead."
  400)
