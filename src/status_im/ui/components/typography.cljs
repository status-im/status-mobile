(ns status-im.ui.components.typography
  (:require [status-im.utils.platform :as platform]
            [status-im.ui.components.colors :as colors]))

(def default-font-family "Inter")
(defn default-style []
  {:color       colors/black
   :font-weight "400"
   :font-size   15})

(defn get-line-height
  [font-size]
  (get {10 14
        11 15
        12 16
        13 17
        14 19
        15 21
        16 22
        17 23
        18 23
        19 24
        20 26
        21 27
        22 28
        23 30
        24 31
        25 32
        26 34
        27 35
        28 35
        29 36
        30 37
        31 38
        32 40
        33 41
        34 42
        40 50}
       font-size))

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
  (let [{:keys [font-weight font-style font-size line-height]
         :as style}
        (merge (default-style)
               (get typography-styles
                    typography)
               (dissoc style :typography :nested?))]
    (if platform/desktop?
      (assoc style :font-family default-font-family)
      (-> style
          (assoc :font-family
                 (if (= (:font-family style) "monospace")
                   (if platform/ios? "Menlo-Regular" "monospace")
                   (str default-font-family "-"
                        (case font-weight
                          "400" (when-not (= font-style :italic)
                                  "Regular")
                          "500" "Medium"
                          "600" "SemiBold"
                          "700" "Bold")
                        (when (= font-style :italic)
                          "Italic"))))
          (dissoc :font-weight :font-style)))))

(defn get-nested-style
  [{:keys [typography] :as style}]
  {:pre [(or (nil? typography) (contains? typography-styles typography))]}
  (let [{:keys [font-weight font-style font-size] :as style}
        (merge (get typography-styles
                    typography)
               (dissoc style :typography))]
    (if platform/desktop?
      style
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
                      "Italic")))))))
