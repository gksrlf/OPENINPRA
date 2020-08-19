package com.little_wizard.tdc.ui.license;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.little_wizard.tdc.R;
import com.little_wizard.tdc.ui.main.MainActivity;

import org.apache.commons.io.FilenameUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LicenseActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (MainActivity.darkMode) setTheme(R.style.Theme_AppCompat_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.title_activity_license));
        }

        ListView listView;
        ListViewAdapter adapter;
        adapter = new ListViewAdapter();

        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        adapter.addItem(
                "AppCompat",
                "https://developer.android.com/jetpack/androidx/releases/appcompat#1.2.0-alpha01",
                "Copyright (c) 2017 The Android Open Source Project",
                "Apache License 2.0"
        );

        adapter.addItem(
                "Constraint Layout Library",
                "https://developer.android.com/reference/androidx/constraintlayout/widget/ConstraintLayout",
                "Copyright (c) 2017 The Android Open Source Project",
                "Apache License 2.0"
        );

        adapter.addItem(
                "Android Material",
                "https://developer.android.com/guide/topics/ui/look-and-feel",
                "Copyright (c) 2017 The Android Open Source Project",
                "Apache License 2.0 "
        );

        adapter.addItem(
                "Android Preference",
                "https://developer.android.com/jetpack/androidx/releases/preference",
                "Copyright (c) 2017 The Android Open Source Project",
                "Apache License 2.0"
        );

        adapter.addItem(
                "AndroidSlidingUpPanel",
                "https://github.com/umano/AndroidSlidingUpPanel",
                "Copyright (c) 2014 umano",
                "Apache License 2.0 "
        );

        adapter.addItem(
                "aws-sdk-android",
                "https://github.com/aws-amplify/aws-sdk-android",
                "Copyright (c)  2014 Amazon Web Services",
                "Apache License 2.0"
        );

        adapter.addItem(
                "Commons IO",
                "https://github.com/apache/commons-io",
                "Copyright (c) 2007 apache",
                "Apache License 2.0 "
        );

        adapter.addItem(
                "glide",
                "https://github.com/bumptech/glide",
                "Copyright (c) 2014 Google, Inc. All rights reserved.",
                "BSD, part MIT and Apache 2.0 License"
        );

        adapter.addItem(
                "RxJava",
                "https://github.com/ReactiveX/RxJava",
                "Copyright (c) 2014 ReactiveX",
                "Apache License 2.0"
        );

        adapter.addItem(
                "butterknife",
                "https://github.com/JakeWharton/butterknife",
                "Copyright (c) 2013 JakeWharton",
                "Apache License 2.0 "
        );

        adapter.addItem(
                "android-3D-model-viewer",
                "https://github.com/andresoviedo/android-3D-model-viewer",
                "Copyright (c) andresoviedo",
                "GNU General Public License version 3.0 (GPLv3)"
        );

        adapter.addItem(
                "Apache License 2.0",
                "Apache License\n" +
                        "                           Version 2.0, January 2004\n" +
                        "                        http://www.apache.org/licenses/\n" +
                        "\n" +
                        "   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION\n" +
                        "\n" +
                        "   1. Definitions.\n" +
                        "\n" +
                        "      \"License\" shall mean the terms and conditions for use, reproduction,\n" +
                        "      and distribution as defined by Sections 1 through 9 of this document.\n" +
                        "\n" +
                        "      \"Licensor\" shall mean the copyright owner or entity authorized by\n" +
                        "      the copyright owner that is granting the License.\n" +
                        "\n" +
                        "      \"Legal Entity\" shall mean the union of the acting entity and all\n" +
                        "      other entities that control, are controlled by, or are under common\n" +
                        "      control with that entity. For the purposes of this definition,\n" +
                        "      \"control\" means (i) the power, direct or indirect, to cause the\n" +
                        "      direction or management of such entity, whether by contract or\n" +
                        "      otherwise, or (ii) ownership of fifty percent (50%) or more of the\n" +
                        "      outstanding shares, or (iii) beneficial ownership of such entity.\n" +
                        "\n" +
                        "      \"You\" (or \"Your\") shall mean an individual or Legal Entity\n" +
                        "      exercising permissions granted by this License.\n" +
                        "\n" +
                        "      \"Source\" form shall mean the preferred form for making modifications,\n" +
                        "      including but not limited to software source code, documentation\n" +
                        "      source, and configuration files.\n" +
                        "\n" +
                        "      \"Object\" form shall mean any form resulting from mechanical\n" +
                        "      transformation or translation of a Source form, including but\n" +
                        "      not limited to compiled object code, generated documentation,\n" +
                        "      and conversions to other media types.\n" +
                        "\n" +
                        "      \"Work\" shall mean the work of authorship, whether in Source or\n" +
                        "      Object form, made available under the License, as indicated by a\n" +
                        "      copyright notice that is included in or attached to the work\n" +
                        "      (an example is provided in the Appendix below).\n" +
                        "\n" +
                        "      \"Derivative Works\" shall mean any work, whether in Source or Object\n" +
                        "      form, that is based on (or derived from) the Work and for which the\n" +
                        "      editorial revisions, annotations, elaborations, or other modifications\n" +
                        "      represent, as a whole, an original work of authorship. For the purposes\n" +
                        "      of this License, Derivative Works shall not include works that remain\n" +
                        "      separable from, or merely link (or bind by name) to the interfaces of,\n" +
                        "      the Work and Derivative Works thereof.\n" +
                        "\n" +
                        "      \"Contribution\" shall mean any work of authorship, including\n" +
                        "      the original version of the Work and any modifications or additions\n" +
                        "      to that Work or Derivative Works thereof, that is intentionally\n" +
                        "      submitted to Licensor for inclusion in the Work by the copyright owner\n" +
                        "      or by an individual or Legal Entity authorized to submit on behalf of\n" +
                        "      the copyright owner. For the purposes of this definition, \"submitted\"\n" +
                        "      means any form of electronic, verbal, or written communication sent\n" +
                        "      to the Licensor or its representatives, including but not limited to\n" +
                        "      communication on electronic mailing lists, source code control systems,\n" +
                        "      and issue tracking systems that are managed by, or on behalf of, the\n" +
                        "      Licensor for the purpose of discussing and improving the Work, but\n" +
                        "      excluding communication that is conspicuously marked or otherwise\n" +
                        "      designated in writing by the copyright owner as \"Not a Contribution.\"\n" +
                        "\n" +
                        "      \"Contributor\" shall mean Licensor and any individual or Legal Entity\n" +
                        "      on behalf of whom a Contribution has been received by Licensor and\n" +
                        "      subsequently incorporated within the Work.\n" +
                        "\n" +
                        "   2. Grant of Copyright License. Subject to the terms and conditions of\n" +
                        "      this License, each Contributor hereby grants to You a perpetual,\n" +
                        "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n" +
                        "      copyright license to reproduce, prepare Derivative Works of,\n" +
                        "      publicly display, publicly perform, sublicense, and distribute the\n" +
                        "      Work and such Derivative Works in Source or Object form.\n" +
                        "\n" +
                        "   3. Grant of Patent License. Subject to the terms and conditions of\n" +
                        "      this License, each Contributor hereby grants to You a perpetual,\n" +
                        "      worldwide, non-exclusive, no-charge, royalty-free, irrevocable\n" +
                        "      (except as stated in this section) patent license to make, have made,\n" +
                        "      use, offer to sell, sell, import, and otherwise transfer the Work,\n" +
                        "      where such license applies only to those patent claims licensable\n" +
                        "      by such Contributor that are necessarily infringed by their\n" +
                        "      Contribution(s) alone or by combination of their Contribution(s)\n" +
                        "      with the Work to which such Contribution(s) was submitted. If You\n" +
                        "      institute patent litigation against any entity (including a\n" +
                        "      cross-claim or counterclaim in a lawsuit) alleging that the Work\n" +
                        "      or a Contribution incorporated within the Work constitutes direct\n" +
                        "      or contributory patent infringement, then any patent licenses\n" +
                        "      granted to You under this License for that Work shall terminate\n" +
                        "      as of the date such litigation is filed.\n" +
                        "\n" +
                        "   4. Redistribution. You may reproduce and distribute copies of the\n" +
                        "      Work or Derivative Works thereof in any medium, with or without\n" +
                        "      modifications, and in Source or Object form, provided that You\n" +
                        "      meet the following conditions:\n" +
                        "\n" +
                        "      (a) You must give any other recipients of the Work or\n" +
                        "          Derivative Works a copy of this License; and\n" +
                        "\n" +
                        "      (b) You must cause any modified files to carry prominent notices\n" +
                        "          stating that You changed the files; and\n" +
                        "\n" +
                        "      (c) You must retain, in the Source form of any Derivative Works\n" +
                        "          that You distribute, all copyright, patent, trademark, and\n" +
                        "          attribution notices from the Source form of the Work,\n" +
                        "          excluding those notices that do not pertain to any part of\n" +
                        "          the Derivative Works; and\n" +
                        "\n" +
                        "      (d) If the Work includes a \"NOTICE\" text file as part of its\n" +
                        "          distribution, then any Derivative Works that You distribute must\n" +
                        "          include a readable copy of the attribution notices contained\n" +
                        "          within such NOTICE file, excluding those notices that do not\n" +
                        "          pertain to any part of the Derivative Works, in at least one\n" +
                        "          of the following places: within a NOTICE text file distributed\n" +
                        "          as part of the Derivative Works; within the Source form or\n" +
                        "          documentation, if provided along with the Derivative Works; or,\n" +
                        "          within a display generated by the Derivative Works, if and\n" +
                        "          wherever such third-party notices normally appear. The contents\n" +
                        "          of the NOTICE file are for informational purposes only and\n" +
                        "          do not modify the License. You may add Your own attribution\n" +
                        "          notices within Derivative Works that You distribute, alongside\n" +
                        "          or as an addendum to the NOTICE text from the Work, provided\n" +
                        "          that such additional attribution notices cannot be construed\n" +
                        "          as modifying the License.\n" +
                        "\n" +
                        "      You may add Your own copyright statement to Your modifications and\n" +
                        "      may provide additional or different license terms and conditions\n" +
                        "      for use, reproduction, or distribution of Your modifications, or\n" +
                        "      for any such Derivative Works as a whole, provided Your use,\n" +
                        "      reproduction, and distribution of the Work otherwise complies with\n" +
                        "      the conditions stated in this License.\n" +
                        "\n" +
                        "   5. Submission of Contributions. Unless You explicitly state otherwise,\n" +
                        "      any Contribution intentionally submitted for inclusion in the Work\n" +
                        "      by You to the Licensor shall be under the terms and conditions of\n" +
                        "      this License, without any additional terms or conditions.\n" +
                        "      Notwithstanding the above, nothing herein shall supersede or modify\n" +
                        "      the terms of any separate license agreement you may have executed\n" +
                        "      with Licensor regarding such Contributions.\n" +
                        "\n" +
                        "   6. Trademarks. This License does not grant permission to use the trade\n" +
                        "      names, trademarks, service marks, or product names of the Licensor,\n" +
                        "      except as required for reasonable and customary use in describing the\n" +
                        "      origin of the Work and reproducing the content of the NOTICE file.\n" +
                        "\n" +
                        "   7. Disclaimer of Warranty. Unless required by applicable law or\n" +
                        "      agreed to in writing, Licensor provides the Work (and each\n" +
                        "      Contributor provides its Contributions) on an \"AS IS\" BASIS,\n" +
                        "      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or\n" +
                        "      implied, including, without limitation, any warranties or conditions\n" +
                        "      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A\n" +
                        "      PARTICULAR PURPOSE. You are solely responsible for determining the\n" +
                        "      appropriateness of using or redistributing the Work and assume any\n" +
                        "      risks associated with Your exercise of permissions under this License.\n" +
                        "\n" +
                        "   8. Limitation of Liability. In no event and under no legal theory,\n" +
                        "      whether in tort (including negligence), contract, or otherwise,\n" +
                        "      unless required by applicable law (such as deliberate and grossly\n" +
                        "      negligent acts) or agreed to in writing, shall any Contributor be\n" +
                        "      liable to You for damages, including any direct, indirect, special,\n" +
                        "      incidental, or consequential damages of any character arising as a\n" +
                        "      result of this License or out of the use or inability to use the\n" +
                        "      Work (including but not limited to damages for loss of goodwill,\n" +
                        "      work stoppage, computer failure or malfunction, or any and all\n" +
                        "      other commercial damages or losses), even if such Contributor\n" +
                        "      has been advised of the possibility of such damages.\n" +
                        "\n" +
                        "   9. Accepting Warranty or Additional Liability. While redistributing\n" +
                        "      the Work or Derivative Works thereof, You may choose to offer,\n" +
                        "      and charge a fee for, acceptance of support, warranty, indemnity,\n" +
                        "      or other liability obligations and/or rights consistent with this\n" +
                        "      License. However, in accepting such obligations, You may act only\n" +
                        "      on Your own behalf and on Your sole responsibility, not on behalf\n" +
                        "      of any other Contributor, and only if You agree to indemnify,\n" +
                        "      defend, and hold each Contributor harmless for any liability\n" +
                        "      incurred by, or claims asserted against, such Contributor by reason\n" +
                        "      of your accepting any such warranty or additional liability.\n" +
                        "\n" +
                        "   END OF TERMS AND CONDITIONS\n" +
                        "\n" +
                        "   APPENDIX: How to apply the Apache License to your work.\n" +
                        "\n" +
                        "      To apply the Apache License to your work, attach the following\n" +
                        "      boilerplate notice, with the fields enclosed by brackets \"[]\"\n" +
                        "      replaced with your own identifying information. (Don't include\n" +
                        "      the brackets!)  The text should be enclosed in the appropriate\n" +
                        "      comment syntax for the file format. We also recommend that a\n" +
                        "      file or class name and description of purpose be included on the\n" +
                        "      same \"printed page\" as the copyright notice for easier\n" +
                        "      identification within third-party archives.\n" +
                        "\n" +
                        "   Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "   you may not use this file except in compliance with the License.\n" +
                        "   You may obtain a copy of the License at\n" +
                        "\n" +
                        "       http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "   Unless required by applicable law or agreed to in writing, software\n" +
                        "   distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "   See the License for the specific language governing permissions and\n" +
                        "   limitations under the License.\n"
        );

        adapter.addItem(
                "MIT License",
                "The MIT License (MIT)\n" +
                        "\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n"
        );

        adapter.addItem(
                "GNU General Public License version 3.0 (GPLv3)",
                "\n" +
                        "   GNU LESSER GENERAL PUBLIC LICENSE\n" +
                        "                       Version 3, 29 June 2007\n" +
                        "\n" +
                        " Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>\n" +
                        " Everyone is permitted to copy and distribute verbatim copies\n" +
                        " of this license document, but changing it is not allowed.\n" +
                        "\n" +
                        "\n" +
                        "  This version of the GNU Lesser General Public License incorporates\n" +
                        "the terms and conditions of version 3 of the GNU General Public\n" +
                        "License, supplemented by the additional permissions listed below.\n" +
                        "\n" +
                        "  0. Additional Definitions.\n" +
                        "\n" +
                        "  As used herein, \"this License\" refers to version 3 of the GNU Lesser\n" +
                        "General Public License, and the \"GNU GPL\" refers to version 3 of the GNU\n" +
                        "General Public License.\n" +
                        "\n" +
                        "  \"The Library\" refers to a covered work governed by this License,\n" +
                        "other than an Application or a Combined Work as defined below.\n" +
                        "\n" +
                        "  An \"Application\" is any work that makes use of an interface provided\n" +
                        "by the Library, but which is not otherwise based on the Library.\n" +
                        "Defining a subclass of a class defined by the Library is deemed a mode\n" +
                        "of using an interface provided by the Library.\n" +
                        "\n" +
                        "  A \"Combined Work\" is a work produced by combining or linking an\n" +
                        "Application with the Library.  The particular version of the Library\n" +
                        "with which the Combined Work was made is also called the \"Linked\n" +
                        "Version\".\n" +
                        "\n" +
                        "  The \"Minimal Corresponding Source\" for a Combined Work means the\n" +
                        "Corresponding Source for the Combined Work, excluding any source code\n" +
                        "for portions of the Combined Work that, considered in isolation, are\n" +
                        "based on the Application, and not on the Linked Version.\n" +
                        "\n" +
                        "  The \"Corresponding Application Code\" for a Combined Work means the\n" +
                        "object code and/or source code for the Application, including any data\n" +
                        "and utility programs needed for reproducing the Combined Work from the\n" +
                        "Application, but excluding the System Libraries of the Combined Work.\n" +
                        "\n" +
                        "  1. Exception to Section 3 of the GNU GPL.\n" +
                        "\n" +
                        "  You may convey a covered work under sections 3 and 4 of this License\n" +
                        "without being bound by section 3 of the GNU GPL.\n" +
                        "\n" +
                        "  2. Conveying Modified Versions.\n" +
                        "\n" +
                        "  If you modify a copy of the Library, and, in your modifications, a\n" +
                        "facility refers to a function or data to be supplied by an Application\n" +
                        "that uses the facility (other than as an argument passed when the\n" +
                        "facility is invoked), then you may convey a copy of the modified\n" +
                        "version:\n" +
                        "\n" +
                        "   a) under this License, provided that you make a good faith effort to\n" +
                        "   ensure that, in the event an Application does not supply the\n" +
                        "   function or data, the facility still operates, and performs\n" +
                        "   whatever part of its purpose remains meaningful, or\n" +
                        "\n" +
                        "   b) under the GNU GPL, with none of the additional permissions of\n" +
                        "   this License applicable to that copy.\n" +
                        "\n" +
                        "  3. Object Code Incorporating Material from Library Header Files.\n" +
                        "\n" +
                        "  The object code form of an Application may incorporate material from\n" +
                        "a header file that is part of the Library.  You may convey such object\n" +
                        "code under terms of your choice, provided that, if the incorporated\n" +
                        "material is not limited to numerical parameters, data structure\n" +
                        "layouts and accessors, or small macros, inline functions and templates\n" +
                        "(ten or fewer lines in length), you do both of the following:\n" +
                        "\n" +
                        "   a) Give prominent notice with each copy of the object code that the\n" +
                        "   Library is used in it and that the Library and its use are\n" +
                        "   covered by this License.\n" +
                        "\n" +
                        "   b) Accompany the object code with a copy of the GNU GPL and this license\n" +
                        "   document.\n" +
                        "\n" +
                        "  4. Combined Works.\n" +
                        "\n" +
                        "  You may convey a Combined Work under terms of your choice that,\n" +
                        "taken together, effectively do not restrict modification of the\n" +
                        "portions of the Library contained in the Combined Work and reverse\n" +
                        "engineering for debugging such modifications, if you also do each of\n" +
                        "the following:\n" +
                        "\n" +
                        "   a) Give prominent notice with each copy of the Combined Work that\n" +
                        "   the Library is used in it and that the Library and its use are\n" +
                        "   covered by this License.\n" +
                        "\n" +
                        "   b) Accompany the Combined Work with a copy of the GNU GPL and this license\n" +
                        "   document.\n" +
                        "\n" +
                        "   c) For a Combined Work that displays copyright notices during\n" +
                        "   execution, include the copyright notice for the Library among\n" +
                        "   these notices, as well as a reference directing the user to the\n" +
                        "   copies of the GNU GPL and this license document.\n" +
                        "\n" +
                        "   d) Do one of the following:\n" +
                        "\n" +
                        "       0) Convey the Minimal Corresponding Source under the terms of this\n" +
                        "       License, and the Corresponding Application Code in a form\n" +
                        "       suitable for, and under terms that permit, the user to\n" +
                        "       recombine or relink the Application with a modified version of\n" +
                        "       the Linked Version to produce a modified Combined Work, in the\n" +
                        "       manner specified by section 6 of the GNU GPL for conveying\n" +
                        "       Corresponding Source.\n" +
                        "\n" +
                        "       1) Use a suitable shared library mechanism for linking with the\n" +
                        "       Library.  A suitable mechanism is one that (a) uses at run time\n" +
                        "       a copy of the Library already present on the user's computer\n" +
                        "       system, and (b) will operate properly with a modified version\n" +
                        "       of the Library that is interface-compatible with the Linked\n" +
                        "       Version.\n" +
                        "\n" +
                        "   e) Provide Installation Information, but only if you would otherwise\n" +
                        "   be required to provide such information under section 6 of the\n" +
                        "   GNU GPL, and only to the extent that such information is\n" +
                        "   necessary to install and execute a modified version of the\n" +
                        "   Combined Work produced by recombining or relinking the\n" +
                        "   Application with a modified version of the Linked Version. (If\n" +
                        "   you use option 4d0, the Installation Information must accompany\n" +
                        "   the Minimal Corresponding Source and Corresponding Application\n" +
                        "   Code. If you use option 4d1, you must provide the Installation\n" +
                        "   Information in the manner specified by section 6 of the GNU GPL\n" +
                        "   for conveying Corresponding Source.)\n" +
                        "\n" +
                        "  5. Combined Libraries.\n" +
                        "\n" +
                        "  You may place library facilities that are a work based on the\n" +
                        "Library side by side in a single library together with other library\n" +
                        "facilities that are not Applications and are not covered by this\n" +
                        "License, and convey such a combined library under terms of your\n" +
                        "choice, if you do both of the following:\n" +
                        "\n" +
                        "   a) Accompany the combined library with a copy of the same work based\n" +
                        "   on the Library, uncombined with any other library facilities,\n" +
                        "   conveyed under the terms of this License.\n" +
                        "\n" +
                        "   b) Give prominent notice with the combined library that part of it\n" +
                        "   is a work based on the Library, and explaining where to find the\n" +
                        "   accompanying uncombined form of the same work.\n" +
                        "\n" +
                        "  6. Revised Versions of the GNU Lesser General Public License.\n" +
                        "\n" +
                        "  The Free Software Foundation may publish revised and/or new versions\n" +
                        "of the GNU Lesser General Public License from time to time. Such new\n" +
                        "versions will be similar in spirit to the present version, but may\n" +
                        "differ in detail to address new problems or concerns.\n" +
                        "\n" +
                        "  Each version is given a distinguishing version number. If the\n" +
                        "Library as you received it specifies that a certain numbered version\n" +
                        "of the GNU Lesser General Public License \"or any later version\"\n" +
                        "applies to it, you have the option of following the terms and\n" +
                        "conditions either of that published version or of any later version\n" +
                        "published by the Free Software Foundation. If the Library as you\n" +
                        "received it does not specify a version number of the GNU Lesser\n" +
                        "General Public License, you may choose any version of the GNU Lesser\n" +
                        "General Public License ever published by the Free Software Foundation.\n" +
                        "\n" +
                        "  If the Library as you received it specifies that a proxy can decide\n" +
                        "whether future versions of the GNU Lesser General Public License shall\n" +
                        "apply, that proxy's public statement of acceptance of any version is\n" +
                        "permanent authorization for you to choose that version for the\n" +
                        "Library.\n"
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}