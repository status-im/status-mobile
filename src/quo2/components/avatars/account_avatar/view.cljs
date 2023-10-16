(ns quo2.components.avatars.account-avatar.view
  (:require
    [clojure.string :as string]
    [quo2.components.avatars.account-avatar.style :as style]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  "Opts:
   
    :type  - keyword -> :default/:watch-only/:missing-keypair

    :emoji - string -> 🍑 [default]
   
    :size  - keyword -> :size-80 [default] :size-80/:size-64/:size-48/:size-32/...
   
    :customization-color - keyword or hexstring -> :blue/:army/... or #ABCEDF
   
    :theme - keyword -> :light/:dark"
  [{:keys [size emoji]
    :or   {size  style/default-size
           emoji "🍑"}
    :as   opts}]
  (let [emoji-size (style/get-emoji-size size)]
    [rn/view
     {:accessible          true
      :accessibility-label :account-avatar
      :style               (style/root-container opts)}
     [rn/text
      {:accessibility-label      :account-emoji
       :adjusts-font-size-to-fit true
       :style                    {:font-size emoji-size}}
      (string/trim emoji)]]))

(def view (quo.theme/with-theme view-internal))
