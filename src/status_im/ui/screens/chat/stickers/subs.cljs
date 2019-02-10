(ns status-im.ui.screens.chat.stickers.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.extensions.add.subs))

(re-frame/reg-sub
 :stickers/selected-pack
 (fn [db]
   (get db :stickers/selected-pack)))

