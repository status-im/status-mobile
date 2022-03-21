(ns status-im.ui.components.typography
  (:require [quo.design-system.colors :as colors]))

(def default-font-family "Inter")
(defn default-style []
  {:color       colors/black
   :font-weight "400"
   :font-size   15
   :line-height 20})

(def typography-styles
  {:header        {:font-weight "700"
                   :font-size   22}

   :title-bold    {:font-weight "700"
                   :font-size   17}

   :title         {:font-size   17}

   :main-semibold {:font-weight "600"}

   :main-medium   {:font-weight "500"}

   :caption       {:font-size   12}

   :timestamp     {:font-size      10
                   :text-transform :uppercase}})

(defn get-style
  [{:keys [typography] :as style}]
  {:pre [(or (nil? typography) (contains? typography-styles typography))]}
  (let [{:keys [font-weight font-style]
         :as style}
        (merge (default-style)
               (get typography-styles
                    typography)
               (dissoc style :typography :nested?))]
    (-> style
        (assoc :font-family
               (str default-font-family "-"
                    (case font-weight
                      "400" (when-not (= font-style :italic)
                              "Regular")
                      "500" "Medium"
                      "600" "SemiBold"
                      "700" "Bold")
                    (when (= font-style :italic)
                      "Italic")))
        (dissoc :font-weight :font-style))))

(defn get-nested-style
  [{:keys [typography] :as style}]
  {:pre [(or (nil? typography) (contains? typography-styles typography))]}
  (let [{:keys [font-weight font-style] :as style}
        (merge (get typography-styles
                    typography)
               (dissoc style :typography))]
    (cond-> (dissoc style :font-weight :font-style)
      (or font-weight font-style)
      (assoc :font-family
             (str default-font-family "-"
                  (case font-weight
                    "500" "Medium"
                    "600" "SemiBold"
                    "700" "Bold"
                    (when-not (= font-style :italic)
                      "Regular"))
                  (when (= font-style :italic)
                    "Italic"))))))
