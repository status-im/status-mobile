(ns quo.components.avatars.account-avatar.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.account-avatar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]))

(defn- view-pure
  "Opts:
   
    :type  - keyword -> :default/:watch-only

    :emoji - string -> 🍑 [default]
   
    :size  - number -> 80 [default] /48/32/28/24/20/16
   
    :customization-color - keyword or hexstring -> :blue/:army/... or #ABCEDF
   
    :theme - keyword -> :light/:dark"
  [{:keys [size emoji]
    :or   {size  style/default-size
           emoji "🍑"}
    :as   opts}]
  (let [theme      (quo.theme/use-theme)
        emoji-size (style/get-emoji-size size)]
    (rn.pure/view
     {:accessible          true
      :accessibility-label :account-avatar
      :style               (style/root-container opts theme)}
     (rn.pure/text
      {:accessibility-label      :account-emoji
       :adjusts-font-size-to-fit true
       :style                    {:font-size emoji-size}}
      (when emoji (string/trim emoji))))))

(defn view [params] (rn.pure/func view-pure params))
