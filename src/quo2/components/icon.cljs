(ns quo2.components.icon
  (:require
   [quo.theme :as theme]
   [status-im.ui.components.icons.icons :as icons]))

(defn icon
  ([icon-name] (icon icon-name nil))
  ([icon-name {:keys [size] :as props}]
   (let [size (or size 20)]
     [icons/icon (str (name icon-name) size) (merge props
                                                    {:width size
                                                     :height size})])))
(defn icon-for-theme
  ([icon-name theme]
   (icon-for-theme icon-name theme nil))
  ([icon-name theme props]
   (let [theme-icon-name (if (= theme :dark)
                           (str (name icon-name) "-dark")
                           icon-name)]
     (icon theme-icon-name props))))

(defn theme-icon
  ([icon-name]
   (theme-icon icon-name nil))
  ([icon-name props]
   (icon-for-theme icon-name (theme/get-theme) props)))
