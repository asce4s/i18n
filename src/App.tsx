import { useTranslation } from "react-i18next";
import "./App.css";
import i18n from "./i18n";

function App() {
  i18n.on("languageChanged", (language) => {
    console.log("languageChanged", language);
  });

  const { t } = useTranslation();
  return (
    <>
      <div>
        <button onClick={() => i18n.changeLanguage("en")}>English</button>
        &nbsp;&nbsp;
        <button onClick={() => i18n.changeLanguage("fr")}>French</button>
        <h1>{t("welcome")}</h1>
        <p>{t("this_is_a_test")}</p>
      </div>
    </>
  );
}

export default App;
