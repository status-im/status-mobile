(ns status-im.ui.screens.extensions.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.extensions.add.subs))

(re-frame/reg-sub
 :get-extensions
 (fn [db]
   (seq (:registry db))))
