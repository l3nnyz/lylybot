// Interactions de la page principale (aucune dépendance).
(() => {
    const $ = (id) => document.getElementById(id);
    const app = $("app");
    const saisie = $("saisie");
    const envoyer = $("btn-envoyer");
    const fil = $("fil");
    const historique = $("historique");
    const reduit = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const stocke = {
        lire: (k) => { try { return localStorage.getItem(k); } catch { return null; } },
        ecrire: (k, v) => { try { localStorage.setItem(k, v); } catch { /* ignoré */ } },
    };

    // --- Menu latéral : réduire / ouvrir ----------------------------------
    const btnMenu = $("btn-menu");
    function regleMenu(ferme) {
        app.classList.toggle("menu-ferme", ferme);
        btnMenu.setAttribute("aria-expanded", String(!ferme));
        stocke.ecrire("lylybot.menu", ferme ? "ferme" : "ouvert");
    }
    regleMenu(stocke.lire("lylybot.menu") === "ferme");
    btnMenu.addEventListener("click", () => regleMenu(!app.classList.contains("menu-ferme")));

    // --- Recherche dans l'historique ---------------------------------------
    const filtre = $("filtre");
    $("btn-recherche").addEventListener("click", () => {
        const ouvert = filtre.classList.toggle("ouvert");
        if (ouvert) filtre.focus(); else { filtre.value = ""; filtrer(); }
    });
    function filtrer() {
        const q = filtre.value.trim().toLowerCase();
        historique.querySelectorAll(".app-conv").forEach((a) => {
            a.hidden = q !== "" && !a.textContent.toLowerCase().includes(q);
        });
    }
    filtre.addEventListener("input", filtrer);

    // --- Groupes repliables --------------------------------------------------
    historique.addEventListener("click", (e) => {
        const g = e.target.closest(".app-groupe");
        if (!g) return;
        g.classList.toggle("replie");
        let n = g.nextElementSibling;
        while (n && n.classList.contains("app-conv")) { n.classList.toggle("cache", g.classList.contains("replie")); n = n.nextElementSibling; }
    });

    // --- Encart astuce / cloche ----------------------------------------------
    const astuce = $("astuce");
    astuce?.querySelector(".app-astuce-x").addEventListener("click", () => {
        astuce.classList.add("sortie");
        setTimeout(() => astuce.remove(), reduit ? 0 : 300);
    });
    $("btn-cloche").addEventListener("click", (e) => e.currentTarget.classList.add("lue"));

    // --- Zone de saisie ---------------------------------------------------------
    function ajuste() {
        saisie.style.height = "auto";
        saisie.style.height = Math.min(saisie.scrollHeight, 200) + "px";
        envoyer.disabled = saisie.value.trim() === "";
    }
    saisie.addEventListener("input", ajuste);
    saisie.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) { e.preventDefault(); $("formulaire").requestSubmit(); }
    });
    saisie.focus();

    // Suggestions : pré-remplissent la saisie
    $("suggestions").addEventListener("click", (e) => {
        const b = e.target.closest("[data-texte]");
        if (!b) return;
        saisie.value = b.dataset.texte;
        ajuste();
        saisie.focus();
        saisie.setSelectionRange(saisie.value.length, saisie.value.length);
    });

    // Texte d'exemple qui change dans la zone vide
    const exemples = ["Demandez n'importe quoi", "Expliquez-moi la photosynthèse simplement", "Aidez-moi à écrire un e-mail", "Donnez-moi des idées de recettes"];
    let ex = 0;
    if (!reduit) {
        setInterval(() => {
            if (saisie.value || document.activeElement === saisie || app.classList.contains("en-cours")) return;
            ex = (ex + 1) % exemples.length;
            saisie.placeholder = exemples[ex];
        }, 3500);
    }

    // --- Conversation (démonstration locale : pas encore reliée à un modèle) ---
    let titreAjoute = false;
    function bulle(role, texte) {
        const d = document.createElement("div");
        d.className = "msg msg-" + role;
        d.textContent = texte;
        fil.appendChild(d);
        fil.scrollTo({ top: fil.scrollHeight, behavior: reduit ? "auto" : "smooth" });
        return d;
    }
    function ajouteHistorique(titre) {
        const a = document.createElement("a");
        a.href = "#";
        a.className = "app-conv nouvelle";
        a.textContent = titre.length > 40 ? titre.slice(0, 40) + "…" : titre;
        const groupe = $("groupe-aujourdhui");
        if (groupe) groupe.after(a); else historique.prepend(a);
        historique.querySelector(".app-vide")?.remove();
    }

    $("formulaire").addEventListener("submit", (e) => {
        e.preventDefault();
        const texte = saisie.value.trim();
        if (!texte) return;
        app.classList.add("en-cours");
        bulle("moi", texte);
        if (!titreAjoute) { ajouteHistorique(texte); titreAjoute = true; }
        saisie.value = "";
        ajuste();

        const frappe = document.createElement("div");
        frappe.className = "msg msg-frappe";
        frappe.innerHTML = '<img src="/img/loading.gif" alt="Réponse en cours">';
        fil.appendChild(frappe);
        fil.scrollTo({ top: fil.scrollHeight, behavior: "smooth" });
        setTimeout(() => {
            frappe.remove();
            const m = document.querySelector(".app-select option:checked")?.textContent ?? "le modèle";
            bulle("ia", "Aperçu de l'interface : cette réponse est simulée. Les vrais échanges avec " + m + " arriveront quand l'API sera branchée.");
        }, reduit ? 0 : 1400);
    });

    $("btn-nouvelle").addEventListener("click", (e) => {
        e.preventDefault();
        app.classList.remove("en-cours");
        fil.replaceChildren();
        titreAjoute = false;
        saisie.value = "";
        ajuste();
        saisie.focus();
    });
})();
