# Dental Tax Reference — Java Module for Russian Tax Authority Reports

> **Modernization of a legacy Delphi module** for generating tax deduction reports in the **Dental4Windows** medical information system.

## 🎯 Project Goal

The original tax reference module for the Russian Federal Tax Service (FTS) was built in **Delphi**, relied on **INI configuration files**, and offered limited customization.

This **Java-based rewrite** provides:

- **User-friendly GUI** for configuration (replaces manual INI editing)
- **Cross-platform compatibility** (Windows, Linux, macOS)
- **Modern standards support**: PDF/A reports, barcodes (ZXing), digital signatures
- **CI/CD & container-ready** (Docker, GitHub Actions)

The application complies with:
- **Russian Tax Code** (Article 219, para. 1, subpara. 3)
- **FTS Order No. EA-7-11/8240** on tax deduction report format

---

## Universal for Any Clinic

The app is **not hardcoded to specific procedure names** (e.g., "Orthodontics").

On first launch, it:
1. Connects to the **Dental4Windows database** (SQL Anywhere)
2. Loads available procedure categories from `general_procedures_lev_2`
3. Lets the user select relevant categories via the UI

This ensures compatibility with **any dental clinic**, regardless of their Dental4Windows setup.

---

## Docker Support

The project includes a `Dockerfile` for containerization, demonstrating:

- Application packaging
- Dependency isolation
- CI/CD integration readiness

> **Note**: The GUI is not rendered in headless Docker environments, but the architecture supports full automation and deployment scenarios.

### Build and run

```bash
docker build -t dental-tax-reference .
docker run --rm dental-tax-reference